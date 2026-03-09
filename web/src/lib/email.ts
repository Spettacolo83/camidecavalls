import nodemailer from "nodemailer";

const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST,
  port: Number(process.env.SMTP_PORT) || 587,
  secure: Number(process.env.SMTP_PORT) === 465,
  auth: {
    user: process.env.SMTP_USER,
    pass: process.env.SMTP_PASS,
  },
});

export async function sendInviteEmail(
  to: string,
  name: string,
  inviteToken: string,
  tempPassword: string
) {
  const baseUrl = process.env.AUTH_URL || "http://localhost:3000";
  const inviteUrl = `${baseUrl}/invite/${inviteToken}`;

  await transporter.sendMail({
    from: process.env.EMAIL_FROM || "noreply@followtheflowai.com",
    to,
    subject: "You've been invited to Camí de Cavalls Admin",
    html: `
      <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; background: #1C1C2E; border-radius: 16px; overflow: hidden;">
        <div style="padding: 32px 32px 24px; text-align: center; border-bottom: 1px solid #3A3A55;">
          <h1 style="margin: 0; color: #4FC3F7; font-size: 22px;">Camí de Cavalls</h1>
          <p style="margin: 8px 0 0; color: #8E8E9A; font-size: 13px;">Admin Panel</p>
        </div>
        <div style="padding: 32px;">
          <p style="color: #E8E8F0; font-size: 15px; margin: 0 0 16px;">
            Hi <strong>${name}</strong>,
          </p>
          <p style="color: #E8E8F0; font-size: 15px; margin: 0 0 24px;">
            You've been invited to join the Camí de Cavalls admin panel. Click the button below to activate your account.
          </p>
          <div style="text-align: center; margin: 0 0 24px;">
            <a href="${inviteUrl}" style="display: inline-block; padding: 12px 32px; background: #4FC3F7; color: #1C1C2E; font-weight: 600; text-decoration: none; border-radius: 8px; font-size: 15px;">
              Accept Invite
            </a>
          </div>
          <div style="background: #252540; border-radius: 8px; padding: 16px; margin: 0 0 24px;">
            <p style="color: #8E8E9A; font-size: 13px; margin: 0 0 8px;">Your temporary credentials:</p>
            <p style="color: #E8E8F0; font-size: 14px; margin: 0;">
              <strong>Email:</strong> ${to}<br/>
              <strong>Password:</strong> <code style="background: #1C1C2E; padding: 2px 6px; border-radius: 4px;">${tempPassword}</code>
            </p>
          </div>
          <p style="color: #8E8E9A; font-size: 12px; margin: 0;">
            If the button doesn't work, copy and paste this URL:<br/>
            <a href="${inviteUrl}" style="color: #4FC3F7; word-break: break-all;">${inviteUrl}</a>
          </p>
        </div>
      </div>
    `,
  });
}
