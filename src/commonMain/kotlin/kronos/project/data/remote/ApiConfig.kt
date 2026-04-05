package kronos.project.data.remote

expect object ApiConfig {
    /**
     * Common default for local development (Desktop/Web).
     * Android emulator should use 10.0.2.2 (see androidMain override if present).
     */
    val BASE_URL: String
}
