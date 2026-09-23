package pe.upeu.biblioandes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform