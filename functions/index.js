const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

const DEAD_TOKEN_CODES = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument",
]);

exports.onCrewMessageCreated = onDocumentCreated(
  {
    document: "crews/{crewId}/messages/{messageId}",
    region: "europe-west1",
  },
  async (event) => {
    const message = event.data && event.data.data();
    if (!message || !message.text) return;

    const crewId = event.params.crewId;
    const authorId = message.authorId;

    const crewSnap = await db.doc(`crews/${crewId}`).get();
    if (!crewSnap.exists) return;

    const crewName = crewSnap.get("name") || "";
    const memberIds = (crewSnap.get("memberIds") || []).filter(
      (uid) => uid && uid !== authorId
    );
    if (memberIds.length === 0) return;

    const tokenDocs = [];
    await Promise.all(
      memberIds.map(async (uid) => {
        const snap = await db.collection(`users/${uid}/tokens`).get();
        snap.forEach((doc) => tokenDocs.push({ uid, token: doc.id }));
      })
    );

    if (tokenDocs.length === 0) {
      logger.info(`No device tokens for crew ${crewId}`);
      return;
    }

    const tokens = tokenDocs.map((entry) => entry.token);

    const response = await messaging.sendEachForMulticast({
      tokens,
      notification: {
        title: crewName || message.authorName || "Renown",
        body: `${message.authorName}: ${message.text}`,
      },
      data: {
        type: "crew_message",
        crewId,
        authorId: authorId || "",
        authorName: message.authorName || "",
        text: message.text,
        crewName,
        sentAtMillis: String(message.sentAtMillis || Date.now()),
      },
      android: {
        priority: "high",
        notification: {
          channelId: "crew_chat",
          tag: "crew_chat",
        },
      },
    });

    const stale = [];
    response.responses.forEach((result, index) => {
      if (result.success) return;
      const code = result.error && result.error.code;
      logger.warn(`Send failed for ${tokenDocs[index].token}: ${code}`);
      if (DEAD_TOKEN_CODES.has(code)) stale.push(tokenDocs[index]);
    });

    if (stale.length > 0) {
      await Promise.all(
        stale.map((entry) =>
          db.doc(`users/${entry.uid}/tokens/${entry.token}`).delete()
        )
      );
      logger.info(`Removed ${stale.length} stale token(s)`);
    }

    logger.info(
      `crew ${crewId}: sent ${response.successCount}/${tokens.length}`
    );
  }
);
