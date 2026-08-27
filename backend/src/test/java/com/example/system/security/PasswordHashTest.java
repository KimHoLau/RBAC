package com.example.system.security;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

/**
 * The ADMIN_HASH constant below must stay identical to the password hash
 * seeded for user "admin" in backend/db/init.sql.
 */
class PasswordHashTest {

    /** BCrypt hash of the seed admin password "admin123". */
    private static final String ADMIN_HASH = "$2a$10$6Q.0i0obqZEV8ak5bWUpB.7iXDsL4.YDf53SuEJIfw2/eZch/2xDe";

    @Test
    void seededAdminHashMatchesAdmin123() {
        org.junit.jupiter.api.Assertions.assertTrue(BCrypt.checkpw("admin123", ADMIN_HASH));
    }
}
