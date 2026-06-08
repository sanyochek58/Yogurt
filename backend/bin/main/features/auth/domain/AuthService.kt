package com.yogurtvpn.features.auth.domain

import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class AuthService(
    private val repository: AuthRepository,
) {
    suspend fun register(email: String, password: String): User{
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!email.matches(emailRegex)){
            throw IllegalArgumentException("Invalid email format")
        }

        if(password.length < 8){
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        if(repository.existsByEmail(email)){
            throw IllegalArgumentException("User already registered")
        }

        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12))
        return repository.create(email, passwordHash)
    }

    suspend fun login(email: String, password: String): User{
        val user = repository.findByEmail(email) ?: throw IllegalArgumentException("User not found")

        if(!BCrypt.checkpw(password, user.passwordHash)){
            throw IllegalArgumentException("Passwords do not match")
        }

        return user
    }

    suspend fun findById(id: UUID): User = repository.findById(id) ?: throw IllegalArgumentException("User not found")
}