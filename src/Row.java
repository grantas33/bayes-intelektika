public enum Row {
//    HORMONAL_CONTRACEPTIVES (0),
//    IUD (1),
//    DX_CANCER (2),
//    DX (3),
//    DX_HPV (4),
//    NUM_OF_PREGNANCIES(5),
//    AGE(6),
//    DX_CIN(7),

//    HINSELMANN (5),
//    SCHILLER(6),
//    CITOLOGY(7),
//    BIOPSY(8);

//    HINSELMANN (8),
//    SCHILLER(9),
//    CITOLOGY(10),
//    BIOPSY(11);

    HINSELMANN (30),
    SCHILLER(31),
    CITOLOGY(32),
    BIOPSY(33);


    int idx;
    Row(int idx) {
        this.idx = idx;
    }
}
