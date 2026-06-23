package org.tracker.ubus.ubus.Components.Shared.Mail.Templates.PendingAdmin;

public record Templates() {
    /**
     * Builds the admin invitation email HTML content
     *
     * @param invitationCode The unique code the admin needs to enter during registration
     * @param expiryMinutes The number of minutes until the invitation expires
     * @return The complete HTML email as a String
     */
    public static String buildAdminInvitationEmail(String invitationCode, int expiryMinutes) {
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
        html.append("                    You've been granted administrator access to the UBus transit management platform. Use the invitation code below to set up your account and get started.\n");
        html.append("                </p>\n");
        html.append("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"background-color: #f7f7f7; border-radius: 8px; border: 1px solid #e2e2e2; margin-bottom: 32px;\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td align=\"center\" style=\"padding: 24px;\">\n");
        html.append("                            <p style=\"font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.08em; color: #777777; margin: 0 0 8px 0;\">\n");
        html.append("                                Your Invitation Code\n");
        html.append("                            </p>\n");
        html.append("                            <p style=\"font-size: 32px; font-weight: 700; color: #f26522; letter-spacing: 0.05em; margin: 0; font-family: 'Courier New', monospace;\">\n");
        html.append("                                ").append(invitationCode).append("\n");
        html.append("                            </p>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n");
        html.append("                <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"margin-bottom: 32px;\">\n");
        html.append("                    <tr>\n");
        html.append("                        <td style=\"padding: 12px 0;\">\n");
        html.append("                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        html.append("                                <tr>\n");
        html.append("                                    <td style=\"vertical-align: middle; width: 28px;\">\n");
        html.append("                                        <span style=\"display: inline-block; width: 24px; height: 24px; background-color: #f26522; color: #ffffff; border-radius: 50%; text-align: center; font-size: 12px; font-weight: 700; line-height: 24px;\">1</span>\n");
        html.append("                                    </td>\n");
        html.append("                                    <td style=\"padding-left: 12px; font-size: 14px; color: #474747;\">\n");
        html.append("                                        Go to the UBus admin registration page\n");
        html.append("                                    </td>\n");
        html.append("                                </tr>\n");
        html.append("                            </table>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                    <tr>\n");
        html.append("                        <td style=\"padding: 12px 0;\">\n");
        html.append("                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        html.append("                                <tr>\n");
        html.append("                                    <td style=\"vertical-align: middle; width: 28px;\">\n");
        html.append("                                        <span style=\"display: inline-block; width: 24px; height: 24px; background-color: #f26522; color: #ffffff; border-radius: 50%; text-align: center; font-size: 12px; font-weight: 700; line-height: 24px;\">2</span>\n");
        html.append("                                    </td>\n");
        html.append("                                    <td style=\"padding-left: 12px; font-size: 14px; color: #474747;\">\n");
        html.append("                                        Enter your invitation code: <strong style=\"color: #000000;\">").append(invitationCode).append("</strong>\n");
        html.append("                                    </td>\n");
        html.append("                                </tr>\n");
        html.append("                            </table>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                    <tr>\n");
        html.append("                        <td style=\"padding: 12px 0;\">\n");
        html.append("                            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
        html.append("                                <tr>\n");
        html.append("                                    <td style=\"vertical-align: middle; width: 28px;\">\n");
        html.append("                                        <span style=\"display: inline-block; width: 24px; height: 24px; background-color: #f26522; color: #ffffff; border-radius: 50%; text-align: center; font-size: 12px; font-weight: 700; line-height: 24px;\">3</span>\n");
        html.append("                                    </td>\n");
        html.append("                                    <td style=\"padding-left: 12px; font-size: 14px; color: #474747;\">\n");
        html.append("                                        Complete your profile and set up your account\n");
        html.append("                                    </td>\n");
        html.append("                                </tr>\n");
        html.append("                            </table>\n");
        html.append("                        </td>\n");
        html.append("                    </tr>\n");
        html.append("                </table>\n");
        html.append("                <p style=\"font-size: 14px; color: #777777; margin: 32px 0 0 0;\">\n");
        html.append("                    This invitation code expires in <strong style=\"color: #474747;\">").append(expiryMinutes).append(" minutes</strong>. If you weren't expecting this, you can safely ignore this email — no account will be created.\n");
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