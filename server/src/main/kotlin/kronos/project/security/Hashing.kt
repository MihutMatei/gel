package kronos.project.security

import org.mindrot.jbcrypt.BCrypt

object Hashing {
    fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())

    fun verifyPassword(password: String, passwordHash: String): Boolean = runCatching {
        BCrypt.checkpw(password, passwordHash)
    }.getOrDefault(false)
}

