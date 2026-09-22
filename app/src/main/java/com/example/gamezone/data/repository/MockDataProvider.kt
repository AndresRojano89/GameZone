package com.example.gamezone.data.repository

import com.example.gamezone.data.model.Game
import com.example.gamezone.data.model.GameCategory

object MockDataProvider {
    val games = listOf(
        // Featured - Imagen horizontal (Steam Capsule)
        Game(
            id = 1,
            title = "Elden Ring: Shadow of the Erdtree",
            description = "Una expansión masiva que lleva a los jugadores a una nueva tierra llena de misterios, mazmorras peligrosas y nuevos enemigos.",
            rating = 4.9,
            genre = "Action RPG",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1245620/capsule_616x353.jpg",
            category = GameCategory.FEATURED
        ),
        // Trending - Portadas Verticales (Steam Library 600x900)
        Game(
            id = 2,
            title = "God of War Ragnarök",
            description = "Kratos y Atreus deben viajar a cada uno de los Nueve Reinos en busca de respuestas.",
            rating = 4.8,
            genre = "Action Adventure",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/2322010/library_600x900.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 3,
            title = "Cyberpunk 2077",
            description = "Un RPG de acción y aventura en mundo abierto ambientado en Night City.",
            rating = 4.5,
            genre = "RPG",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1091500/library_600x900.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 4,
            title = "Marvel's Spider-Man: Miles Morales",
            description = "Tras los acontecimientos de Marvel's Spider-Man Remastered, Miles Morales se adapta a su nuevo hogar mientras sigue los pasos de Peter Parker como un nuevo Spider-Man.",
            rating = 4.7,
            genre = "Action Adventure",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1817190/library_600x900.jpg",
            category = GameCategory.TRENDING
        ),
        // Top Rated - Portadas Verticales (IMDb stable CDN)
        Game(
            id = 5,
            title = "The Legend of Zelda: Tears of the Kingdom",
            description = "Una aventura épica a través de la tierra y los cielos de Hyrule.",
            rating = 5.0,
            genre = "Adventure",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5vmg.jpg",
            category = GameCategory.TOP_RATED
        ),
        Game(
            id = 6,
            title = "Baldur's Gate 3",
            description = "Reúne a tu grupo y regresa a los Reinos Olvidados en una historia de compañerismo y traición.",
            rating = 4.9,
            genre = "RPG",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1086940/library_600x900.jpg",
            category = GameCategory.TOP_RATED
        ),
        // Coming Soon - Portadas Verticales
        Game(
            id = 7,
            title = "Red Dead Redemption 2",
            description = "Una epopeya de la vida en el implacable corazón de América al final de la era del salvaje oeste.",
            rating = 4.9,
            genre = "Action Adventure",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rcc.jpg",
            category = GameCategory.COMING_SOON
        ),
        Game(
            id = 8,
            title = "Hollow Knight: Silksong",
            description = "Explora un reino vasto y antiguo en la secuela del aclamado Hollow Knight.",
            rating = 0.0,
            genre = "Metroidvania",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1030300/library_600x900.jpg",
            category = GameCategory.COMING_SOON
        ),
        // Retro (Premium) - Portadas Verticales
        Game(
            id = 9,
            title = "Marvel's Spider-Man",
            description = "Ponte en la piel de un Peter Parker experimentado y lucha contra el crimen organizado y villanos icónicos en la Nueva York de Marvel.",
            rating = 4.8,
            genre = "Action Adventure",
            imageUrl = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1817070/library_600x900.jpg",
            isPremium = true,
            category = GameCategory.RETRO
        ),
        Game(
            id = 10,
            title = "The Legend of Zelda: Breath of the Wild",
            description = "Explora un mundo abierto inmenso y lleno de vida.",
            rating = 4.9,
            genre = "Action Adventure",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3p2d.jpg",
            isPremium = true,
            category = GameCategory.RETRO
        ),
        // Nuevos Juegos FASE B
        Game(
            id = 11,
            title = "The Witcher 3: Wild Hunt",
            description = "Conviértete en el legendario cazador de monstruos Geralt de Rivia en un mundo de fantasía devastado por la guerra.",
            rating = 4.9,
            genre = "RPG",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 12,
            title = "Sekiro: Shadows Die Twice",
            description = "Talla tu propio camino de venganza en esta galardonada aventura de acción de FromSoftware.",
            rating = 4.8,
            genre = "Action",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5glk.jpg",
            category = GameCategory.TOP_RATED
        ),
        Game(
            id = 13,
            title = "Resident Evil 4 Remake",
            description = "Leon S. Kennedy se enfrenta a horrores indescriptibles para rescatar a la hija del presidente en una aldea europea aislada.",
            rating = 4.7,
            genre = "Action",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co5u91.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 14,
            title = "Horizon Forbidden West",
            description = "Acompaña a Aloy en su viaje por una frontera majestuosa y peligrosa que oculta nuevas y misteriosas amenazas.",
            rating = 4.6,
            genre = "Action Adventure",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co4hbo.jpg",
            category = GameCategory.TOP_RATED
        ),
        Game(
            id = 15,
            title = "Metroid Dread",
            description = "Samus Aran debe escapar de un planeta alienígena hostil mientras es perseguida por una amenaza mecánica imparable.",
            rating = 4.7,
            genre = "Metroidvania",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3p59.jpg",
            category = GameCategory.TOP_RATED
        ),
        Game(
            id = 16,
            title = "Death Stranding Director's Cut",
            description = "Del legendario creador Hideo Kojima, una experiencia que desafía los géneros para reconectar un mundo fragmentado.",
            rating = 4.5,
            genre = "Action",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co3t4p.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 17,
            title = "Final Fantasy VII Rebirth",
            description = "Cloud y sus amigos abandonan la ciudad de Midgar para explorar el vasto mundo exterior en busca de Sephiroth.",
            rating = 4.9,
            genre = "RPG",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co7e2c.jpg",
            category = GameCategory.TRENDING
        ),
        Game(
            id = 18,
            title = "Metroid Prime Remastered",
            description = "Explora el planeta Tallon IV a través de los ojos de Samus Aran en esta versión mejorada del clásico de culto.",
            rating = 4.8,
            genre = "Adventure",
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/co61at.jpg",
            category = GameCategory.RETRO,
            isPremium = true
        )
    )
}
