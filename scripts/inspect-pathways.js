const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

const KEY_PATH = path.join(__dirname, "serviceAccountKey.json");

if (!fs.existsSync(KEY_PATH)) {
  console.error("serviceAccountKey.json bulunamadi: " + KEY_PATH);
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(KEY_PATH)),
});

const db = admin.firestore();

async function main() {
  const pathways = await db.collection("pathways").get();
  console.log(`pathways: ${pathways.size} dokuman`);

  for (const doc of pathways.docs) {
    const quests = await doc.ref.collection("quests").get();
    const data = doc.data();
    console.log(
      `  ${doc.id.padEnd(28)} "${data.title || data.titleTr || "?"}" ` +
        `-> quests: ${quests.size}`
    );
    if (quests.size > 0) {
      console.log(`     ornek alanlar: ${Object.keys(quests.docs[0].data()).join(", ")}`);
    }
  }

  if (pathways.size > 0) {
    console.log("\npathway dokuman alanlari:", Object.keys(pathways.docs[0].data()).join(", "));
  }

  const catalog = await db.collection("taskCatalog").get();
  console.log(`\ntaskCatalog: ${catalog.size} dokuman`);
  if (catalog.size > 0) {
    const d = catalog.docs[0].data();
    console.log("  alanlar:", Object.keys(d).join(", "));
    const missingEn = catalog.docs.filter(
      (x) => !x.data().titleEn || !x.data().descriptionEn
    ).length;
    console.log(`  ingilizce cevirisi eksik: ${missingEn}/${catalog.size}`);
  }
}

main().then(() => process.exit(0)).catch((e) => {
  console.error(e.message);
  process.exit(1);
});
