package com.example.valtrak.Data.CardLibrary.Vehicles;

import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Vehicles.China.ChineseVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.France.FrenchVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Germany.GermanVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Israel.IsraeliVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Italy.ItalianVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Japan.JapaneseVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Russia.RussianVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.SouthKorea.SouthKoreanVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Support.ReconVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Support.ResupplyVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Support.UavTeams;
import com.example.valtrak.Data.CardLibrary.Vehicles.Sweden.SwedishVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.UK.BritishVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.US.USGroundVehicles;

import java.util.List;

/**
 * Every vehicle card in the game, nation by nation, then the support vehicles. The card loader and the simulator both read
 * this list, so a new nation's enum only has to be added here. Keep new lists at the end: the simulator numbers cards in
 * this order.
 */
public final class VehicleLibrary {
    private VehicleLibrary() {}

    public static final List<GroundVehicleCardInterface[]> ALL = List.of(
            USGroundVehicles.values(), RussianVehicles.values(), GermanVehicles.values(),
            ReconVehicles.values(), UavTeams.values(), ResupplyVehicles.values(),
            JapaneseVehicles.values(), ChineseVehicles.values(), BritishVehicles.values(), FrenchVehicles.values(),
            IsraeliVehicles.values(), SwedishVehicles.values(), ItalianVehicles.values(), SouthKoreanVehicles.values());
}
