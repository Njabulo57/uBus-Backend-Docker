package org.tracker.ubus.ubus.Components.Shared.Mail.Templates.PendingAdmin;

public record Templates() {
    /**
     * Builds the admin invitation email HTML content
     *
     * @param inviteLink The registration link for the admin
     * @param expiryMinutes The number of minutes until the invitation expires
     * @return The complete HTML email as a String
     */
    public static String buildAdminInvitationEmail(String inviteLink, int expiryMinutes) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"utf-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>You've been invited to UBus Admin</title>\n");
        html.append("</head>\n");
        html.append("<body style=\"margin: 0; padding: 0; background-color: #ffffff; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;\">\n");
        html.append("    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff;\">\n");
        html.append("        <tr>\n");
        html.append("            <td style=\"padding: 40px 40px 20px 40px;\">\n");
        html.append("                <span style=\"font-size: 24px; font-weight: bold; color: #000000; letter-spacing: -0.05em;\">\n");
        html.append("                    <span style=\"color: #f26522;\">U</span>Bus\n");
        html.append("                </span>\n");
        html.append("            </td>\n");
        html.append("        </tr>\n");
        html.append("        <tr>\n");
        html.append("            <td style=\"padding: 20px 40px 40px 40px;\">\n");
        html.append("                <p style=\"font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.12em; color: #f26522; margin: 0 0 16px 0;\">\n");
        html.append("                    Admin Invitation\n");
        html.append("                </p>\n");
        html.append("                <h1 style=\"font-size: 36px; font-weight: 800; color: #000000; line-height: 1.1; margin: 0 0 24px 0; letter-spacing: -0.04em;\">\n");
        html.append("                    You've been invited<br/>to manage UBus.\n");
        html.append("                </h1>\n");
        html.append("                <p style=\"font-size: 16px; line-height: 1.6; color: #474747; margin: 0 0 32px 0;\">\n");
        html.append("                    You've been granted administrator access to the UBus transit management platform. Click the button below to set up your account and get started.\n");
        html.append("                </p>\n");
        html.append("\n");
        html.append("                <!-- CTA Button -->\n");
        html.append("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td align=\"center\" style=\"border-radius: 4px; background-color: #f26522;\">\n");
        html.append("                            <a href=\"").append(inviteLink).append("\" target=\"_blank\"\n");
        html.append("                               style=\"display: inline-block; padding: 16px 32px; font-size: 15px; font-weight: 700; color: #ffffff; text-decoration: none; letter-spacing: -0.01em;\">\n");
        html.append("                                Complete Registration →\n");
        html.append("                            </a>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n");
        html.append("\n");
        html.append("                <!-- Fallback URL -->\n");
        html.append("                <p style=\"font-size: 13px; color: #777777; margin: 24px 0 0 0;\">\n");
        html.append("                    Or copy and paste this link into your browser:\n");
        html.append("                </p>\n");
        html.append("                <p style=\"font-size: 13px; color: #474747; margin: 6px 0 0 0; word-break: break-all;\">\n");
        html.append("                    <a href=\"").append(inviteLink).append("\" style=\"color: #f26522; text-decoration: underline;\">").append(inviteLink).append("</a>\n");
        html.append("                </p>\n");
        html.append("\n");
        html.append("                <p style=\"font-size: 14px; color: #777777; margin: 32px 0 0 0;\">\n");
        html.append("                    This invitation link expires in <strong style=\"color: #474747;\">").append(expiryMinutes).append(" minutes</strong>. If you weren't expecting this, you can safely ignore this email — no account will be created.\n");
        html.append("                </p>\n");
        html.append("            </td>\n");
        html.append("        </tr>\n");
        html.append("        <tr>\n");
        html.append("            <td style=\"padding: 0 40px;\">\n");
        html.append("                <div style=\"border-top: 1px solid #e2e2e2; padding-top: 30px;\">\n");
        html.append("                    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n");
        html.append("                        <tr>\n");
        html.append("                            <td style=\"vertical-align: middle; width: 12px;\">\n");
        html.append("                                <div style=\"width: 8px; height: 8px; background-color: #f26522; border-radius: 50%;\"></div>\n");
        html.append("                            </td>\n");
        html.append("                            <td>\n");
        html.append("                                <span style=\"font-size: 10px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.1em; color: #000000; margin-left: 8px;\">\n");
        html.append("                                    Security Status: Invite Pending\n");
        html.append("                                </span>\n");
        html.append("                            </td>\n");
        html.append("                        </tr>\n");
        html.append("                    </table>\n");
        html.append("                </div>\n");
        html.append("            </td>\n");
        html.append("        </tr>\n");
        html.append("        <tr>\n");
        html.append("            <td style=\"padding: 60px 40px 40px 40px;\">\n");
        html.append("                <p style=\"font-size: 11px; font-weight: 500; color: #777777; text-transform: uppercase; letter-spacing: 0.1em; margin: 0 0 12px 0;\">\n");
        html.append("                    ©️ 2026 UBus. All rights reserved.\n");
        html.append("                </p>\n");
        html.append("                <div style=\"font-size: 11px; font-weight: 500; color: #777777; text-transform: uppercase; letter-spacing: 0.1em;\">\n");
        html.append("                    <a href=\"#\" style=\"color: #777777; text-decoration: none; margin-right: 15px;\">Privacy</a>\n");
        html.append("                    <a href=\"#\" style=\"color: #777777; text-decoration: none; margin-right: 15px;\">Terms</a>\n");
        html.append("                    <a href=\"#\" style=\"color: #777777; text-decoration: none;\">Contact</a>\n");
        html.append("                </div>\n");
        html.append("            </td>\n");
        html.append("        </tr>\n");
        html.append("    </table>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }
}
