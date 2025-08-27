package com.mylifeisrpg.myliftisrpg.exception

/**
 * Exception thrown when attempting to register a user with an already existing username
 */
class UserAlreadyExistsException(message: String) : RuntimeException(message)

/**
 * Exception thrown when login credentials are invalid
 */
class InvalidCredentialsException(message: String) : RuntimeException(message)

/**
 * Exception thrown when user is not found
 */
class UserNotFoundException(message: String) : RuntimeException(message)