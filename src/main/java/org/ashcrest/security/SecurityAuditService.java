package org.ashcrest.security;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Security audit service for guild permissions, anti-raid monitoring, and verification checks.
 */
public class SecurityAuditService {

    public record AuditResult(int totalMembers, int dangerousRoleMembers, List<String> warningFlags) {}

    /**
     * Audit server permissions and flag dangerous permission configurations.
     */
    public static AuditResult auditGuild(Guild guild) {
        List<String> warnings = new ArrayList<>();
        int dangerousRoleMembers = 0;

        EnumSet<Permission> dangerousPerms = EnumSet.of(
                Permission.ADMINISTRATOR,
                Permission.MANAGE_SERVER,
                Permission.MANAGE_ROLES,
                Permission.BAN_MEMBERS,
                Permission.KICK_MEMBERS
        );

        for (Member member : guild.getMembers()) {
            boolean isDangerous = false;
            for (Role role : member.getRoles()) {
                if (role.getPermissions().stream().anyMatch(dangerousPerms::contains)) {
                    isDangerous = true;
                    break;
                }
            }
            if (isDangerous) {
                dangerousRoleMembers++;
            }
        }

        // Check @everyone role permissions
        Role everyoneRole = guild.getPublicRole();
        for (Permission perm : dangerousPerms) {
            if (everyoneRole.hasPermission(perm)) {
                warnings.add("⚠️ `@everyone` role has critical permission: " + perm.getName());
            }
        }

        if (dangerousRoleMembers > guild.getMemberCount() * 0.2) {
            warnings.add("⚠️ Over 20% of members hold administrative/moderation permissions.");
        }

        return new AuditResult(guild.getMemberCount(), dangerousRoleMembers, warnings);
    }
}
