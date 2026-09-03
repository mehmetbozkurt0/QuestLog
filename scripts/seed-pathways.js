const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");
const { pathways } = require("./pathways");

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
const APPLY = process.argv.includes("--apply");

function validate() {
  const problems = [];
  const ids = new Set();
  const orders = new Set();

  for (const p of pathways) {
    if (ids.has(p.id)) problems.push(`yol id tekrar ediyor: ${p.id}`);
    ids.add(p.id);
    if (orders.has(p.order)) problems.push(`yol order tekrar ediyor: ${p.order} (${p.id})`);
    orders.add(p.order);
    if (!p.title || !p.primaryStat) problems.push(`${p.id}: title veya primaryStat eksik`);
    if (!p.titleEn || !p.descriptionEn) problems.push(`${p.id}: ingilizce cevirisi eksik`);
    if (!Array.isArray(p.quests) || p.quests.length === 0) problems.push(`${p.id}: gorev yok`);

    const questIds = new Set();
    for (const q of p.quests || []) {
      if (questIds.has(q.id)) problems.push(`${p.id}: gorev id tekrar ediyor: ${q.id}`);
      questIds.add(q.id);
      if (!q.title || !q.statType || !q.difficulty) {
        problems.push(`${q.id}: title, statType veya difficulty eksik`);
      }
      if (!(q.requiredCompletions > 0)) {
        problems.push(`${q.id}: requiredCompletions pozitif olmali`);
      }
      if (!q.titleEn || !q.descriptionEn) {
        problems.push(`${q.id}: ingilizce cevirisi eksik`);
      }
    }
  }

  for (const p of pathways) {
    if (p.requiredPathwayId && !ids.has(p.requiredPathwayId)) {
      problems.push(`${p.id}: onkosul yolu bulunamadi (${p.requiredPathwayId})`);
    }
    if (p.requiredPathwayId === p.id) {
      problems.push(`${p.id}: kendi kendisinin onkosulu olamaz`);
    }
  }

  return problems;
}

async function main() {
  const problems = validate();
  if (problems.length > 0) {
    console.error("Dogrulama basarisiz:");
    problems.forEach((p) => console.error("  - " + p));
    process.exit(1);
  }

  const questCount = pathways.reduce((sum, p) => sum + p.quests.length, 0);
  console.log(`Yerel veri: ${pathways.length} yol, ${questCount} gorev. Dogrulama tamam.`);

  const existing = await db.collection("pathways").get();
  const wanted = new Set(pathways.map((p) => p.id));
  const stalePathways = existing.docs.filter((d) => !wanted.has(d.id));

  const staleQuests = [];
  for (const doc of existing.docs) {
    const local = pathways.find((p) => p.id === doc.id);
    if (!local) continue;
    const wantedQuests = new Set(local.quests.map((q) => q.id));
    const remote = await doc.ref.collection("quests").get();
    remote.docs.filter((q) => !wantedQuests.has(q.id)).forEach((q) => staleQuests.push(q));
  }

  console.log(
    `Firestore: ${existing.size} yol. Silinecek yol: ${stalePathways.length}, ` +
      `silinecek gorev: ${staleQuests.length}.`
  );
  if (stalePathways.length > 0) {
    console.log("  silinecek yollar: " + stalePathways.map((d) => d.id).join(", "));
  }
  if (staleQuests.length > 0) {
    console.log("  silinecek gorevler: " + staleQuests.map((d) => d.id).join(", "));
  }

  if (!APPLY) {
    console.log("\nProva modu. Yazmak icin: node seed-pathways.js --apply");
    return;
  }

  let batch = db.batch();
  let ops = 0;

  const flush = async () => {
    if (ops === 0) return;
    await batch.commit();
    batch = db.batch();
    ops = 0;
  };

  const stage = async (fn) => {
    fn();
    ops++;
    if (ops >= 400) await flush();
  };

  for (const p of pathways) {
    const ref = db.collection("pathways").doc(p.id);
    await stage(() =>
      batch.set(ref, {
        title: p.title,
        description: p.description,
        titleEn: p.titleEn ?? null,
        descriptionEn: p.descriptionEn ?? null,
        primaryStat: p.primaryStat,
        secondaryStat: p.secondaryStat ?? null,
        tier: p.tier,
        requiredPathwayId: p.requiredPathwayId ?? null,
        completionBonusXp: p.completionBonusXp,
        order: p.order,
      })
    );

    for (const q of p.quests) {
      await stage(() =>
        batch.set(ref.collection("quests").doc(q.id), {
          title: q.title,
          description: q.description,
          titleEn: q.titleEn ?? null,
          descriptionEn: q.descriptionEn ?? null,
          statType: q.statType,
          difficulty: q.difficulty,
          stage: q.stage,
          requiredCompletions: q.requiredCompletions,
          order: q.order,
        })
      );
    }
  }

  for (const doc of staleQuests) {
    await stage(() => batch.delete(doc.ref));
  }

  for (const doc of stalePathways) {
    const quests = await doc.ref.collection("quests").get();
    for (const q of quests.docs) {
      await stage(() => batch.delete(q.ref));
    }
    await stage(() => batch.delete(doc.ref));
  }

  await flush();
  console.log(`${pathways.length} yol, ${questCount} gorev yazildi.`);
}

main()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error(err);
    process.exit(1);
  });
