const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.sendNotificationToAll = functions.https.onRequest(async (req, res) => {
  try {
    if (req.method !== "POST") return res.status(405).send("Use POST only");

    const { title, body } = req.body;
    if (!title || !body) return res.status(400).send("Title & Body required");

    // Fetch all tokens from Firestore
    const snapshot = await db.collection("tokens").get();
    const tokens = snapshot.docs.map(doc => doc.data().token);

    if (!tokens.length) return res.status(404).send("No tokens found");

    // Send notification to all tokens
    const message = { notification: { title, body }, tokens };
    const response = await admin.messaging().sendEachForMulticast(message);

    return res.status(200).json({ success: true, results: response });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ error: err.message });
  }
});
