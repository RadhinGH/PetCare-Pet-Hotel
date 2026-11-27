package model;

import java.util.List;

/**
 * Interface: satu "kontrak" untuk semua yang bisa ditagih.
 * GroomingService dan BoardingService sama-sama memenuhi kontrak ini,
 * tetapi cara menghitungnya berbeda (polymorphism).
 */

public interface Tagihan {

    double hitungTotalBiaya();

    List<BarisTagihan> rincianTagihan();
}
