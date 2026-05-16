package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;
import com.example.valtrak.Data.GameData.DataTransfer.DamageData.CombatResult;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.AttackRequest;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.CreateGameRequest;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.DeployRequest;
import com.example.valtrak.Data.GameData.DataTransfer.GameData.PlayResourceRequest;
import com.example.valtrak.Data.GameData.Entity.GameState.*;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.GameNotFoundException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.InvalidGameActionException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.PlayerNotFoundException;
import com.example.valtrak.Data.GameData.Enums.GamePhase;
import com.example.valtrak.Data.GameData.Enums.GameStatus;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.VehicleCardRepository;
import com.example.valtrak.Data.GameData.Repository.EnumData.VehicleAttackRepository;
import com.example.valtrak.Data.GameData.Repository.GameState.*;
import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final int HAND_SIZE = 7;
    private static final int STARTING_SUPPLY = 10;
    private static final int WIN_CHIPS = 5;

    private final PlayerRepository playerRepo;
    private final GameRepository gameRepo;
    private final PlayerGameStateRepository pgsRepo;
    private final FieldUnitRepository fieldUnitRepo;
    private final StrikeGroupRepository strikeGroupRepo;
    private final CardRepository cardRepo;
    private final VehicleCardRepository vehicleCardRepo;
    private final VehicleAttackRepository vehicleAttackRepo;
    private final CombatService combatService;

    @Transactional
    public Game createGame(CreateGameRequest req) {
        if (req.player1Id().equals(req.player2Id())) {
            throw new InvalidGameActionException("A player cannot play against themselves.");
        }

        var player1 = playerRepo.findById(req.player1Id())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + req.player1Id()));
        var player2 = playerRepo.findById(req.player2Id())
                .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + req.player2Id()));

        validateDeck(req.player1DeckCardIds());
        validateDeck(req.player2DeckCardIds());

        var p1State = buildInitialState(player1, req.player1DeckCardIds());
        var p2State = buildInitialState(player2, req.player2DeckCardIds());

        pgsRepo.save(p1State);
        pgsRepo.save(p2State);

        var game = new Game(player1, player2, p1State, p2State);
        return gameRepo.save(game);
    }

    public Game getGame(Long gameId) {
        return gameRepo.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    @Transactional
    public Game advancePhase(Long gameId, Long playerId) {
        var game = requireActiveGame(gameId);
        requireActiveTurn(game, playerId);

        return switch (game.getCurrentPhase()) {
            case LOGISTICS -> {
                game.setCurrentPhase(GamePhase.COMMAND);
                yield gameRepo.save(game);
            }
            case COMMAND -> {
                game.setCurrentPhase(GamePhase.ENGAGEMENT);
                yield gameRepo.save(game);
            }
            case ENGAGEMENT -> {
                endTurn(game);
                yield gameRepo.save(game);
            }
        };
    }

    @Transactional
    public Game playResource(Long gameId, Long playerId, PlayResourceRequest req) {
        var game = requireActiveGame(gameId);
        requireActiveTurn(game, playerId);
        requirePhase(game, GamePhase.LOGISTICS);

        var state = game.getActivePlayerState();
        Long cardId = requireCardInHand(state, req.cardId());

        Card card = cardRepo.findById(cardId)
                .orElseThrow(() -> new InvalidGameActionException("Card not found: " + cardId));

        if (!(card instanceof ItemCard itemCard)) {
            throw new InvalidGameActionException("Card " + cardId + " is not a resource card.");
        }

        applyItemEffect(state, itemCard);

        state.getHand().remove(cardId);
        state.getDiscard().add(cardId);
        pgsRepo.save(state);
        return game;
    }

    @Transactional
    public FieldUnit deploy(Long gameId, Long playerId, DeployRequest req) {
        var game = requireActiveGame(gameId);
        requireActiveTurn(game, playerId);
        requirePhase(game, GamePhase.COMMAND);

        var state = game.getActivePlayerState();
        Long cardId = requireCardInHand(state, req.cardId());

        Card card = cardRepo.findById(cardId)
                .orElseThrow(() -> new InvalidGameActionException("Card not found: " + cardId));

        if (!(card instanceof GroundVehicleCard vehicleCard)) {
            throw new InvalidGameActionException("Card " + cardId + " is not a deployable vehicle.");
        }

        int cost = getDeployCost(vehicleCard.getLevel());
        if (state.getSupplyPool() < cost) {
            throw new InvalidGameActionException(
                    "Not enough supply to deploy " + vehicleCard.getName() + ". Need " + cost + ", have " + state.getSupplyPool() + "."
            );
        }
        state.setSupplyPool(state.getSupplyPool() - cost);

        StrikeGroup group = resolveStrikeGroup(state, req.strikeGroupId());
        if (group.isFull()) {
            throw new InvalidGameActionException("Strike Group " + group.getId() + " is full (max " + StrikeGroup.MAX_SIZE + " units).");
        }

        FieldUnit unit = new FieldUnit(group, vehicleCard);
        group.getUnits().add(unit);
        fieldUnitRepo.save(unit);

        state.getHand().remove(cardId);
        pgsRepo.save(state);
        return unit;
    }

    @Transactional
    public CombatResult attack(Long gameId, Long playerId, AttackRequest req) {
        var game = requireActiveGame(gameId);
        requireActiveTurn(game, playerId);
        requirePhase(game, GamePhase.ENGAGEMENT);

        var attackerState = game.getActivePlayerState();
        var defenderState = game.getOpponentState();

        FieldUnit attacker = fieldUnitRepo.findById(req.attackerFieldUnitId())
                .orElseThrow(() -> new InvalidGameActionException("Attacking unit not found: " + req.attackerFieldUnitId()));

        FieldUnit target = fieldUnitRepo.findById(req.targetFieldUnitId())
                .orElseThrow(() -> new InvalidGameActionException("Target unit not found: " + req.targetFieldUnitId()));

        validateUnitOwnership(attacker, attackerState, "Attacker");
        validateUnitOwnership(target, defenderState, "Target");

        AttackSlot slot;
        try {
            slot = AttackSlot.valueOf(req.attackSlot());
        } catch (IllegalArgumentException e) {
            throw new InvalidGameActionException("Invalid attack slot: " + req.attackSlot());
        }

        validateAttackerStatus(attacker, slot);

        VehicleAttackEntity attackDef = vehicleAttackRepo
                .findByVehicleAndAttackSlot(attacker.getVehicleCard(), slot)
                .orElseThrow(() -> new InvalidGameActionException(
                        attacker.getVehicleCard().getName() + " has no " + slot + " attack."
                ));

        Ammunition ammo;
        try {
            ammo = Ammunition.valueOf(req.ammoType());
        } catch (IllegalArgumentException e) {
            throw new InvalidGameActionException("Unknown ammo type: " + req.ammoType());
        }

        Weapon weaponEnum = Weapon.valueOf(attackDef.getWeapon().getWeaponName());
        if (!weaponEnum.getCompatibleAmmunition().contains(ammo)) {
            throw new InvalidGameActionException(
                    ammo + " is not compatible with " + weaponEnum.name() + "."
            );
        }

        int ammoCost = attackDef.getAmmoCost();
        int currentAmmo = attackerState.getAmmoInventory().getOrDefault(ammo.name(), 0);
        if (currentAmmo < ammoCost) {
            throw new InvalidGameActionException(
                    "Not enough " + ammo + " ammo. Need " + ammoCost + ", have " + currentAmmo + "."
            );
        }

        CombatResult result = combatService.calculateDamage(
                attackDef, ammo, target.getVehicleCard().getVehicleArmor(), target.getBreachStacks()
        );

        // Deduct ammo
        attackerState.getAmmoInventory().merge(ammo.name(), -ammoCost, Integer::sum);
        pgsRepo.save(attackerState);

        // Apply damage
        target.setCurrentHp(target.getCurrentHp() - result.getFinalDamage());

        // Apply special effect to target
        applyEffect(target, result.getTriggeredEffect());

        if (target.isDestroyed()) {
            handleDestruction(target, defenderState, attackerState, game);
        } else {
            fieldUnitRepo.save(target);
        }

        pgsRepo.save(defenderState);
        return result;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private void validateDeck(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new InvalidGameActionException("Deck cannot be empty.");
        }
        if (cardIds.size() > 80) {
            throw new InvalidGameActionException("Deck cannot exceed 80 cards.");
        }
        boolean hasVehicle = cardIds.stream().anyMatch(id ->
                vehicleCardRepo.findById(id).isPresent()
        );
        if (!hasVehicle) {
            throw new InvalidGameActionException("Deck must contain at least one vehicle card.");
        }
    }

    private PlayerGameState buildInitialState(Player player, List<Long> deckCardIds) {
        List<Long> shuffledDeck = new ArrayList<>(deckCardIds);
        Collections.shuffle(shuffledDeck);

        var state = new PlayerGameState(player, shuffledDeck);
        state.setSupplyPool(STARTING_SUPPLY);

        // Deal HAND_SIZE cards, guaranteeing at least 1 vehicle
        dealHand(state);
        return state;
    }

    private void dealHand(PlayerGameState state) {
        List<Long> deck = state.getDeck();
        List<Long> hand = state.getHand();

        int toDeal = Math.min(HAND_SIZE, deck.size());
        for (int i = 0; i < toDeal; i++) {
            hand.add(deck.removeFirst());
        }

        boolean hasVehicle = hand.stream().anyMatch(id -> vehicleCardRepo.findById(id).isPresent());
        if (!hasVehicle) {
            // Find first vehicle in remaining deck and swap with a non-vehicle in hand
            for (int i = 0; i < deck.size(); i++) {
                Long id = deck.get(i);
                if (vehicleCardRepo.findById(id).isPresent()) {
                    // Swap with first non-vehicle in hand
                    for (int j = 0; j < hand.size(); j++) {
                        if (vehicleCardRepo.findById(hand.get(j)).isEmpty()) {
                            deck.set(i, hand.get(j));
                            hand.set(j, id);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private void endTurn(Game game) {
        var nextState = game.getOpponentState();

        // Clear turn-based effects from the incoming player's units
        nextState.getStrikeGroups().stream()
                .flatMap(sg -> sg.getUnits().stream())
                .forEach(FieldUnit::clearTurnEffects);

        // Reset resource pools for next player
        nextState.resetResourcePools();

        // Draw 1 card
        if (!nextState.getDeck().isEmpty()) {
            Long drawn = nextState.getDeck().removeFirst();
            nextState.getHand().add(drawn);
        }

        pgsRepo.save(nextState);

        // Switch active player
        game.setActivePlayerId(game.getOpponent().getId());
        game.setCurrentPhase(GamePhase.LOGISTICS);

        // Increment turn counter every two half-turns (after player2 acts)
        if (game.getActivePlayerId().equals(game.getPlayer1().getId())) {
            game.setTurnNumber(game.getTurnNumber() + 1);
        }
    }

    private void applyItemEffect(PlayerGameState state, ItemCard card) {
        switch (card.getItemType()) {
            case AMMUNITION -> {
                if (card instanceof AmmunitionCard ammoCard) {
                    state.getAmmoInventory().merge(
                            ammoCard.getAmmunition().name(),
                            ammoCard.getCount(),
                            Integer::sum
                    );
                }
            }
            case FUEL -> {
                if (card instanceof FuelCard fuelCard) {
                    state.setFuelPool(state.getFuelPool() + fuelCard.getCount());
                }
            }
            case REPAIR -> {
                if (card instanceof RepairCard repairCard) {
                    state.setRepairPool(state.getRepairPool() + repairCard.getCount());
                }
            }
            case SUPPLY -> state.setSupplyPool(state.getSupplyPool() + 1);
            default -> throw new InvalidGameActionException("Unsupported item type: " + card.getItemType());
        }
    }

    private StrikeGroup resolveStrikeGroup(PlayerGameState state, Long strikeGroupId) {
        if (strikeGroupId != null) {
            StrikeGroup group = strikeGroupRepo.findById(strikeGroupId)
                    .orElseThrow(() -> new InvalidGameActionException("Strike Group not found: " + strikeGroupId));
            if (!group.getPlayerGameState().getId().equals(state.getId())) {
                throw new InvalidGameActionException("Strike Group does not belong to this player.");
            }
            return group;
        }
        // Create a new Strike Group
        var group = new StrikeGroup(state);
        state.getStrikeGroups().add(group);
        return strikeGroupRepo.save(group);
    }

    private void validateUnitOwnership(FieldUnit unit, PlayerGameState expectedState, String role) {
        Long actualPgsId = unit.getStrikeGroup().getPlayerGameState().getId();
        if (!actualPgsId.equals(expectedState.getId())) {
            throw new InvalidGameActionException(role + " unit does not belong to the correct player.");
        }
    }

    private void validateAttackerStatus(FieldUnit attacker, AttackSlot slot) {
        if (attacker.isStunned()) {
            throw new InvalidGameActionException("Unit is stunned and cannot act this turn.");
        }
        if (attacker.isDisabled()) {
            throw new InvalidGameActionException("Unit is disabled and cannot attack.");
        }
        if (attacker.isSuppressed() && slot == AttackSlot.ATTACK_1) {
            throw new InvalidGameActionException("Unit is suppressed and cannot use ATTACK_1 this turn.");
        }
    }

    private void applyEffect(FieldUnit target, SpecialEffect effect) {
        switch (effect) {
            case SUPPRESSION -> target.setSuppressed(true);
            case DISABLE    -> target.setDisabled(true);
            case STUN       -> target.setStunned(true);
            case BREACH     -> target.setBreachStacks(Math.min(target.getBreachStacks() + 1, 3));
            default         -> { /* NONE, PIERCE, OVERPRESSURE — no persistent effect on unit */ }
        }
    }

    private void handleDestruction(FieldUnit target, PlayerGameState defenderState,
                                   PlayerGameState attackerState, Game game) {
        StrikeGroup group = target.getStrikeGroup();
        group.getUnits().remove(target);
        fieldUnitRepo.delete(target);

        if (group.isEmpty()) {
            defenderState.getStrikeGroups().remove(group);
            strikeGroupRepo.delete(group);
            attackerState.setTerritoryChips(attackerState.getTerritoryChips() + 1);
            pgsRepo.save(attackerState);

            if (attackerState.getTerritoryChips() >= WIN_CHIPS) {
                game.setStatus(GameStatus.FINISHED);
                game.setWinnerId(attackerState.getPlayer().getId());
                gameRepo.save(game);
            }
        }
    }

    private int getDeployCost(CardLevel level) {
        return switch (level) {
            case COMMON    -> 1;
            case UNCOMMON  -> 2;
            case RARE      -> 3;
            case EPIC      -> 4;
            case LEGENDARY -> 6;
            case COMMANDER -> 8;
        };
    }

    private Game requireActiveGame(Long gameId) {
        var game = gameRepo.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new InvalidGameActionException("Game " + gameId + " is not active.");
        }
        return game;
    }

    private void requireActiveTurn(Game game, Long playerId) {
        if (!game.getActivePlayerId().equals(playerId)) {
            throw new InvalidGameActionException("It is not player " + playerId + "'s turn.");
        }
    }

    private void requirePhase(Game game, GamePhase expected) {
        if (game.getCurrentPhase() != expected) {
            throw new InvalidGameActionException(
                    "Action requires " + expected + " phase, but current phase is " + game.getCurrentPhase() + "."
            );
        }
    }

    private Long requireCardInHand(PlayerGameState state, Long cardId) {
        if (!state.getHand().contains(cardId)) {
            throw new InvalidGameActionException("Card " + cardId + " is not in your hand.");
        }
        return cardId;
    }
}
