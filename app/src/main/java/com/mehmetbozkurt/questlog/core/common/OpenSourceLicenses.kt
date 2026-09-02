package com.mehmetbozkurt.questlog.core.common

data class LicenseEntry(
    val name: String,
    val license: String,
    val url: String,
)

object OpenSourceLicenses {

    private const val APACHE_2 = "Apache License 2.0"
    private const val MIT = "MIT License"
    private const val BSD_3 = "BSD 3-Clause License"
    private const val OFL = "SIL Open Font License 1.1"
    private const val ANDROID_SDK = "Android Software Development Kit License"

    val entries: List<LicenseEntry> = listOf(
        LicenseEntry(
            "AndroidX / Jetpack Compose",
            APACHE_2,
            "https://developer.android.com/jetpack/androidx",
        ),
        LicenseEntry(
            "Kotlin ve kotlinx",
            APACHE_2,
            "https://github.com/JetBrains/kotlin",
        ),
        LicenseEntry(
            "Compose Multiplatform",
            APACHE_2,
            "https://github.com/JetBrains/compose-multiplatform",
        ),
        LicenseEntry(
            "Dagger / Hilt",
            APACHE_2,
            "https://github.com/google/dagger",
        ),
        LicenseEntry(
            "Accompanist",
            APACHE_2,
            "https://github.com/google/accompanist",
        ),
        LicenseEntry(
            "Coil",
            APACHE_2,
            "https://github.com/coil-kt/coil",
        ),
        LicenseEntry(
            "OkHttp / Okio",
            APACHE_2,
            "https://github.com/square/okhttp",
        ),
        LicenseEntry(
            "gRPC / PerfMark",
            APACHE_2,
            "https://github.com/grpc/grpc-java",
        ),
        LicenseEntry(
            "Gson",
            APACHE_2,
            "https://github.com/google/gson",
        ),
        LicenseEntry(
            "Guava",
            APACHE_2,
            "https://github.com/google/guava",
        ),
        LicenseEntry(
            "Protocol Buffers",
            BSD_3,
            "https://github.com/protocolbuffers/protobuf",
        ),
        LicenseEntry(
            "Checker Framework annotations",
            MIT,
            "https://github.com/typetools/checker-framework",
        ),
        LicenseEntry(
            "Animal Sniffer annotations",
            MIT,
            "https://github.com/mojohaus/animal-sniffer",
        ),
        LicenseEntry(
            "JSpecify",
            APACHE_2,
            "https://github.com/jspecify/jspecify",
        ),
        LicenseEntry(
            "javax.inject / jakarta.inject",
            APACHE_2,
            "https://github.com/jakartaee/inject",
        ),
        LicenseEntry(
            "Firebase Android SDK",
            "$APACHE_2 / $ANDROID_SDK",
            "https://firebase.google.com/terms",
        ),
        LicenseEntry(
            "Google Play services",
            ANDROID_SDK,
            "https://developer.android.com/studio/terms",
        ),
        LicenseEntry(
            "Cinzel",
            OFL,
            "https://fonts.google.com/specimen/Cinzel",
        ),
        LicenseEntry(
            "EB Garamond",
            OFL,
            "https://fonts.google.com/specimen/EB+Garamond",
        ),
        LicenseEntry(
            "Material Symbols",
            APACHE_2,
            "https://fonts.google.com/icons",
        ),
    )
}
