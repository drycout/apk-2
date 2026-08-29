package com.example.data.local

import com.example.data.model.PackagingTypeEntity
import com.example.data.model.PackagingVariantEntity
import com.example.data.model.ProductEntity

object SeedData {
    val initialPackagingTypes = listOf(
        PackagingTypeEntity(
            id_tipe = "Standard",
            harga_tambahan = 0
        ),
        PackagingTypeEntity(
            id_tipe = "Artificial bunga",
            harga_tambahan = 18000
        )
    )

    val initialPackagingVariants = listOf(
        PackagingVariantEntity(
            id_varian = "VAR002",
            id_tipe = "Artificial bunga",
            name = "Biru",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783473322555_artifial_Biru.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783473322555_artifial_Biru.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna biru muda dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan bunga artifisial di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain elegan Artificial bunga.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR003",
            id_tipe = "Artificial bunga",
            name = "Pink",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783477426734_artifial_pink.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783477426734_artifial_pink.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna Merah muda dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan bunga artifisial di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain elegan Artificial bunga.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR004",
            id_tipe = "Artificial bunga",
            name = "Marun",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783476451170_artifial_marun.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/img_1783476451170_artifial_marun.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna Merah marun dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan bunga artifisial di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain elegan Artificial bunga.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR006",
            id_tipe = "Artificial bunga",
            name = "Hijau",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/artifial_hijau.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/artifial_hijau.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna hijau dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan bunga artifisial di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain elegan Artificial bunga.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR005",
            id_tipe = "Artificial bunga",
            name = "Golden brown",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/artifial_gold.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/artifial_gold.jpg\",\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/artifial_gold2.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna gold dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan bunga artifisial di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain elegan Artificial bunga.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR011",
            id_tipe = "Standard",
            name = "Pink",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stpink.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stpink.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna Merah muda dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan pita & bunga di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain minimalis dan elegan.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR012",
            id_tipe = "Standard",
            name = "Marun",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stmarun.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stmarun.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna merah marun dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan pita & bunga di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain minimalis dan elegan.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR013",
            id_tipe = "Standard",
            name = "Hijau",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/sthijau.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/sthijau.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna hijau dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan pita & bunga di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain minimalis dan elegan.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR014",
            id_tipe = "Standard",
            name = "Golden brown",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stgold.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stgold.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna gold dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan pita & bunga di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain minimalis dan elegan.\"]"
        ),
        PackagingVariantEntity(
            id_varian = "VAR015",
            id_tipe = "Standard",
            name = "Navy",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stnavy.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/stnavy.jpg\"]",
            desc = "Kotak seserahan mewah berbahan hardbox kokoh berwarna navy dengan tutup mika transparan yang elegan. Bagian sudut dipercantik ornamen siku emas, serta dipermanis dengan balutan kain tile dan hiasan pita & bunga di bagian atas. Memberikan kesan rapi, eksklusif, dan siap mempercantik momen spesial Anda.",
            featuresJson = "[\"Material berkualitas, kokoh dan tahan.\",\"Ramah lingkungan.\",\"Desain minimalis dan elegan.\"]"
        )
    )

    val initialProducts = listOf(
        ProductEntity(
            id = 1761330047080L,
            name = "Lemper Kipas",
            price = 165000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783422423589_26j9ok_Lemper.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783422423589_26j9ok_Lemper.jpg\"]",
            desc = "Terbuat dari ketan pulen berisi abon atau ayam suwir berbumbu gurih, kemudian dibungkus dengan daun pisang dan dikukus.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761330657461L,
            name = "Bolen pisang",
            price = 160000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426821909_de48kr_bolen.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426821909_de48kr_bolen.jpg\",\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426824212_t8eqlm_bolen2.jpg\"]",
            desc = "Kue pastry berlapis renyah yang berisi irisan pisang dan cokelat atau keju di dalamnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365259154L,
            name = "Bolu gulung Toping",
            price = 250000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426774265_w3ylu_bolugulung2.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426774265_w3ylu_bolugulung2.jpg\",\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426771557_1a3s9_bolugulung.jpg\"]",
            desc = "Kue lembut berbentuk gulungan yang diisi selai manis atau krim lembut, kemudian diberi topping menarik seperti keju, cokelat, atau meses di atasnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365369638L,
            name = "Wingko babat",
            price = 175000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426716685_2k8nk_wingko.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426716685_2k8nk_wingko.jpg\"]",
            desc = "Terbuat dari kelapa parut, tepung ketan, gula, dan santan, kemudian dipanggang hingga beraroma harum dan bertekstur kenyal legit.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365440506L,
            name = "Brownies",
            price = 180000L,
            cat = "Hantaran",
            img = "https://rboiicqwzcdjxhnhkzkl.supabase.co/storage/v1/object/public/product-images/products/1780658419904-8fqh2d50s1e.jpg",
            imagesJson = "[\"https://rboiicqwzcdjxhnhkzkl.supabase.co/storage/v1/object/public/product-images/products/1780658419904-8fqh2d50s1e.jpg\"]",
            desc = "Fudgy brownies adalah jenis brownies yang memiliki tekstur lembut, padat, dan sangat kenyal di dalamnya. Terbuat dari campuran cokelat leleh, mentega, telur, gula, dan sedikit tepung.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365520498L,
            name = "Telur asin",
            price = 225000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426674337_wmrwbl_telurasin.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426674337_wmrwbl_telurasin.jpg\"]",
            desc = "Olahan telur bebek yang diawetkan dengan cara direndam dalam larutan garam atau dibalut adonan garam dan abu, menghasilkan rasa gurih dan asin khas.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365597291L,
            name = "Lumpia Tower",
            price = 160000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg\"]",
            desc = "Sajian lumpia yang disusun tinggi menyerupai tower, berisi sayuran segar, ayam, atau daging cincang dengan kulit lumpia renyah.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365658085L,
            name = "Kue thok",
            price = 160000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426621990_wk3z0h_kuethok.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426621990_wk3z0h_kuethok.jpg\"]",
            desc = "Berbahan dasar tepung ketan dan isian kacang hijau kupas, dibentuk tebal dan padat dengan tekstur kenyal dan lembut.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365742441L,
            name = "Putu ayu - Saus gula merah",
            price = 137500L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426604773_r1ygr_PutuayuSausgulamerah.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426604773_r1ygr_PutuayuSausgulamerah.jpg\"]",
            desc = "Kue tradisional lembut berbahan dasar tepung terigu, kelapa parut, dan santan, dengan bentuk cantik berlapis kelapa di atasnya. Disajikan bersama saus gula merah kental dan manis.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365789770L,
            name = "Jadah Ketan",
            price = 180000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426499371_tmmzzv_jadah.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426499371_tmmzzv_jadah.jpg\"]",
            desc = "Makanan ini terbuat dari ketan (beras ketan) yang dimasak dengan kelapa parut, memberikan rasa gurih yang lezat dan dipadatkan, lalu dibentuk menjadi bentuk hati.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761365848544L,
            name = "Putu Ayu clasic",
            price = 125000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426480491_4l2egg_PutuAyuclasic.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426480491_4l2egg_PutuAyuclasic.jpg\"]",
            desc = "Kue tradisional lembut berbahan dasar tepung terigu, kelapa parut, dan santan, dengan bentuk cantik berlapis kelapa di atasnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761366079491L,
            name = "Wajik ketan - gula merah",
            price = 200000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426428495_roaeiz_wajik.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426428495_roaeiz_wajik.jpg\"]",
            desc = "Terbuat dari beras ketan dan gula merah, menghasilkan rasa manis legit dengan aroma khas kelapa dan karamel dari gula merah. Teksturnya lengket dan kenyal, mirip dengan jadah, tetapi wajik biasanya lebih manis dan padat.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761366457110L,
            name = "Puding susu clasic + Vla",
            price = 146000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426389742_myd62h_pudingsusu.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426389742_myd62h_pudingsusu.jpg\"]",
            desc = "Terbuat dari susu murni, agar-agar, dan sedikit gula, menghasilkan rasa manis ringan dan creamy. Disajikan dengan vla vanila lembut di atasnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761366531770L,
            name = "Caramel",
            price = 120000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426374775_0zuipb_caramel.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426374775_0zuipb_caramel.jpg\"]",
            desc = "Terbuat dari gula yang dimasak hingga menjadi karamel, kemudian dicampur dengan adonan tepung dan telur.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761368585958L,
            name = "Pie buah",
            price = 162000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426115622_bqk1oy_bikang.jpeg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426115622_bqk1oy_bikang.jpeg\"]",
            desc = "Kue tart mini dengan kulit pastry renyah yang diisi vla lembut dan dihias dengan aneka potongan buah segar di atasnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761368682877L,
            name = "Pie brownies",
            price = 148000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426329673_0aj599_piebrownies.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426329673_0aj599_piebrownies.jpg\"]",
            desc = "Perpaduan lezat antara kulit pie renyah dan isian brownies cokelat lembut di dalamnya.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761368763118L,
            name = "Onde - onde",
            price = 137500L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426263535_ajasrp_onde.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426263535_ajasrp_onde.jpg\"]",
            desc = "Jajanan tradisional berbentuk bulat yang terbuat dari tepung ketan, berisi kacang hijau manis, dan dilapisi taburan wijen di bagian luar.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761369387340L,
            name = "Puff Pastry",
            price = 137500L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426217406_mev0a_pastry2.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426217406_mev0a_pastry2.jpg\",\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426214009_1c5vbno_pastry.jpg\"]",
            desc = "Pastry berlapis yang dibuat dari adonan tepung dan mentega sehingga menghasilkan tekstur ringan, renyah, dan berlapis-lapis saat dipanggang.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761369459952L,
            name = "Dadar gulung mawar",
            price = 137500L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426155189_bnsov8_dadargulungmawar.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426155189_bnsov8_dadargulungmawar.jpg\"]",
            desc = "Dadar gulung pandan yang diisi kelapa parut manis dan dibentuk menyerupai bunga mawar cantik.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761369522206L,
            name = "Bikang",
            price = 125000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426115622_bqk1oy_bikang.jpeg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426115622_bqk1oy_bikang.jpeg\"]",
            desc = "Kue tradisional berbentuk bunga besar atau motif unik, terbuat dari tepung beras, santan, dan gula.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761369803702L,
            name = "Risol Tower",
            price = 160000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg\"]",
            desc = "Sajian Risol yang disusun tinggi menyerupai tower, berisi sayuran segar, ayam, atau daging cincang dengan kulit lumpia renyah.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761369883058L,
            name = "Sosis solo Tower",
            price = 170000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426053285_1acwgh_soslo.jpg\"]",
            desc = "Sajian sosis solo yang disusun tinggi menyerupai tower, ayam cincang dengan kulit dadar gurih renyah.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761370004874L,
            name = "Pastel",
            price = 125000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426016732_nnk3pa6_pastel.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783426016732_nnk3pa6_pastel.jpg\"]",
            desc = "Kue goreng berbentuk setengah lingkaran dengan kulit tipis renyah dan isi sayuran, telur, ayam berbumbu gurih.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761370218863L,
            name = "Wajik + Jadah",
            price = 380000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425958007_20f8i_wajik_jadah.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425958007_20f8i_wajik_jadah.jpg\"]",
            desc = "Kue tradisional wajik manis legit dan jadah gurih lembut, terbuat dari beras ketan, gula merah, parutan kelapa dan santan.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761370289022L,
            name = "Lumpur Kentang",
            price = 148000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425890396_6nsbe_lumpurkentang.jpeg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425890396_6nsbe_lumpurkentang.jpeg\"]",
            desc = "Kue tradisional lembut yang terbuat dari kentang, tepung, telur, dan gula, menghasilkan tekstur kenyal dan lembut.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1761370438578L,
            name = "Parcell Buah Polynett",
            price = 300000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425799545_1z0bt8_Parcell_Buah_Polynett.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425799545_1z0bt8_Parcell_Buah_Polynett.jpg\"]",
            desc = "Rangkaian buah segar pilihan yang disusun cantik dalam keranjang atau kemasan elegan, cocok untuk hadiah, ucapan selamat, atau hampers acara spesial.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1782210194360L,
            name = "Parcell Buah Tyle",
            price = 250000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425715756_eq8z6n_Parcell_Buah_Tyle.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/gal_1783425715756_eq8z6n_Parcell_Buah_Tyle.jpg\"]",
            desc = "Rangkaian buah segar pilihan yang disusun cantik dalam keranjang kain tyle elegan, cocok untuk hadiah, ucapan selamat, atau hampers acara spesial.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1782210534128L,
            name = "Floss roll",
            price = 160000L,
            cat = "Hantaran",
            img = "https://rboiicqwzcdjxhnhkzkl.supabase.co/storage/v1/object/public/product-images/products/1782818932920-oxq8hi3tm5f.webp",
            imagesJson = "[\"https://rboiicqwzcdjxhnhkzkl.supabase.co/storage/v1/object/public/product-images/products/1782818932920-oxq8hi3tm5f.webp\"]",
            desc = "Roti gulung panggang dengan isian abon gurih manis.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1785811228412L,
            name = "Parcell Sembako",
            price = 165000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/sembako.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/sembako.jpg\"]",
            desc = "Rangkaian gula, kopi bubuk hitam, kopi bubuk putih, dan teh yang disusun cantik dalam keranjang atau kemasan elegan, cocok untuk hadiah atau hampers.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        ),
        ProductEntity(
            id = 1785811251530L,
            name = "Parcell Pisang",
            price = 150000L,
            cat = "Hantaran",
            img = "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/pisang.jpg",
            imagesJson = "[\"https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/pisang.jpg\"]",
            desc = "Rangkaian pisang segar pilihan yang disusun cantik dalam keranjang atau kemasan elegan, cocok untuk hadiah, ucapan selamat, atau seserahan acara spesial.",
            bestseller = false,
            isNew = false,
            isPromo = false,
            isOutOfStock = false
        )
    )
}
