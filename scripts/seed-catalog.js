const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");
const { tasks } = require("./catalog-tasks");

const KEY_PATH = path.join(__dirname, "serviceAccountKey.json");

if (!fs.existsSync(KEY_PATH)) {
  console.error("serviceAccountKey.json bulunamadi: " + KEY_PATH);
  console.error("Firebase Console > Proje ayarlari > Hizmet hesaplari > Yeni ozel anahtar olustur");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(require(KEY_PATH)),
});

const db = admin.firestore();

async function main() {
  const existing = await db.collection("taskCatalog").get();
  const wanted = new Set(tasks.map((t) => t.id));
  const stale = existing.docs.filter((d) => !wanted.has(d.id));

  let batch = db.batch();
  let ops = 0;

  for (const task of tasks) {
    batch.set(db.collection("taskCatalog").doc(task.id), {
      id: task.id,
      title: task.title,
      description: task.description,
      titleEn: task.titleEn || null,
      descriptionEn: task.descriptionEn || null,
      statType: task.statType,
      difficulty: task.difficulty,
      sortOrder: task.sortOrder,
    });
    ops++;
  }

  for (const doc of stale) {
    batch.delete(doc.ref);
    ops++;
  }

  await batch.commit();
  console.log(`${tasks.length} gorev yazildi, ${stale.length} eski kayit silindi (${ops} islem).`);
  if (stale.length > 0) {
    console.log("Silinenler: " + stale.map((d) => d.id).join(", "));
  }
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error(err);
    process.exit(1);
  });
