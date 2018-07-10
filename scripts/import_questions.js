'use strict'

// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const admin = require('firebase-admin'),
    striptags = require('striptags'),
    fs = require('fs');

const credentials = require('./data/quiznerd-49e4f-b97ae890548f.json');
const PATH_CATEGORIES = './data/quizzes.json';
const PATH_QUESTIONS = './data/questions.json';
const CREATOR_ID = 'vPFq7bm4MX5EyecNSlvn';
const RANDOM_STRING_LENGTH = 20;
const LIMIT = 80; // Number.MAX_VALUE
const SHUFFLE = true;
const DUMP_JSON = false;

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

let db = admin.firestore();
let collection = db.collection('questions');

const raw = {
    categories: require(PATH_CATEGORIES),
    questions: require(PATH_QUESTIONS)
};

const categoryAliases = {
    'Android': 'ANDROID',
    'C++': 'CPP',
    'C#': 'CSHARP',
    'Html': 'HTML',
    'JavaScript': 'JS',
    'PHP': 'PHP',
    'Python': 'PYTHON',
    'Swift': 'SWIFT',
    'Java': 'JAVA'
};

let categoryMap = Object.assign({}, ...raw.categories
    .filter(k => categoryAliases.hasOwnProperty(k.name))
    .map(k => ({ [k.id]: categoryAliases[k.name] })));

let questions = raw.questions
    .filter(q => q.type == 'single_select')
    .filter(q => categoryMap.hasOwnProperty(q.quiz_id))
    .filter(q => q.options.length >= 2 && q.options.length <= 4)
    .map(q => {
        let question = {
            text: strip(q.question),
            code: q.code,
            category: categoryMap[q.quiz_id],
            creatorId: CREATOR_ID,
            random: randomString(RANDOM_STRING_LENGTH),
            updated: new Date(),
            answers: []
        }

        for (let i = 0; i < q.options.length; i++) {
            question.answers.push({
                id: i,
                text: strip(q.options[i]),
                correct: i == q.answer[0]
            })
        }

        return question
    });

if (SHUFFLE) questions = shuffle(questions);
if (DUMP_JSON) fs.writeFileSync('data/out.json', JSON.stringify(questions, null, 4));

console.log(`Extracted ${questions.length} questions.`)
Object.values(categoryMap).forEach(c => {
    let sub = questions.filter(q => q.category === c)
    console.log(`${c}: ${sub.length}`)
});

let promises = [];
for (let i = 0; i < LIMIT; i++) {
    promises.push(() => {
        let q = questions[i];
        console.log(`Creating question ${i} of category ${q.category}.`);
        return collection.doc(i.toString()).set(q);
    });
}

promiseSerial(promises)
    .then(console.log.bind(console))
    .catch(console.error.bind(console))

/* FUNCTIONS */

function promiseSerial(funcs) {
    return funcs.reduce((promise, func) =>
        promise.then(result => func().then(Array.prototype.concat.bind(result))),
        Promise.resolve([]));
}

function strip(text) {
    return striptags(text).replace(/&.+;/g, '')
}

/* https://stackoverflow.com/a/1349426/3112139 */
function randomString(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < length; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    return text;
}

function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
}