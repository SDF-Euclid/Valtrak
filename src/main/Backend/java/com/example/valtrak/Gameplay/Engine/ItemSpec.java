package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;

/**
 * An item card. What {@code power} and {@code count} mean depends on the effect:
 * <ul>
 *   <li>ERA: {@code power} = percent less CHEMICAL damage</li>
 *   <li>ARTILLERY: {@code power} = base damage to each target (worked out with the rules' Artillery damage type), {@code count} = how many targets</li>
 *   <li>SEARCH: {@code count} = how many cards, {@code searchKind} = what kind</li>
 *   <li>DRAW, SABOTAGE, RECYCLE: {@code count} = how many cards</li>
 *   <li>SMOKE: {@code count} = how many vehicles</li>
 *   <li>JAMMER: {@code power} = Fuel upkeep each turn while it is on, {@code count} = its HP</li>
 *   <li>CAMO: {@code power} = Fuel less to retreat</li>
 *   <li>AIRDROP: {@code count} = how many resource cards</li>
 *   <li>RAPID_DEPLOY: {@code count} = how many vehicles
 * </ul>
 */
public record ItemSpec(long cardId, String name, CardLevel level, ItemEffect effect,
                       int power, int count, SearchKind searchKind) implements CardSpec {}
