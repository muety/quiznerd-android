'use strict'

// Add questions from JSON (stored in data/new) according to template data/question.json.tpl
// Each question has to be a separate file

// Get credentials from https://console.cloud.google.com/iam-admin/serviceaccounts?project=quiznerd-49e4f&authuser=0

const RANDOM_STRING_LENGTH = 20
const CREATOR_ID = 'vPFq7bm4MX5EyecNSlvn'
const admin = require('firebase-admin'),
    fs = require('fs');

const credentials = require('./data/quiznerd-49e4f-firebase-adminsdk-3kssd-3b1663ae73.json')

admin.initializeApp({
    credential: admin.credential.cert(credentials)
});

let db = admin.firestore();
let questions = db.collection('questions')

let dataDir = 'data/new'
let filelist = fs.readdirSync(dataDir)
let newQuestions = filelist.map(f => JSON.parse(fs.readFileSync(`${dataDir}/${f}`)))

getMaxInc()
    .then(incs => {
        let promises = []
        for (let i = 0; i < newQuestions.length; i++) {
            promises.push(() => {
                let index = i + incs.inc + 1;
                let q = newQuestions[i]
                q.inc = index
                q.catInc = incs.catInc[q.category] + i + 1
                q.random = randomString(RANDOM_STRING_LENGTH)
                q.updated = new Date()
                q.creatorId = CREATOR_ID
                console.log(`Creating question ${index} of category ${q.category}.`)
                return questions.doc(index.toString()).set(q)
            })
        }

        promiseSerial(promises)
            .then(console.log.bind(console))
            .catch(console.error.bind(console))
    })
    .catch(console.err)

function getMaxInc() {
    return questions.get()
        .then(snapshot => {
            let incs = {
                catInc: {},
                inc: 0
            }

            snapshot.forEach(doc => {
                let data = doc.data()
                if (data.hasOwnProperty('inc') && data.inc > incs.inc) {
                    incs.inc = data.inc
                }
                if (data.hasOwnProperty('catInc')) {
                    if (!incs.catInc.hasOwnProperty(data.category)) {
                        incs.catInc[data.category] = 0
                    }
                    if (data.catInc > incs.catInc[data.category]) {
                        incs.catInc[data.category] = incs.catInc[data.category]
                    }
                }
            })

            return incs
        })
}

function promiseSerial(funcs) {
    return funcs.reduce((promise, func) =>
        promise.then(result => func().then(Array.prototype.concat.bind(result))),
        Promise.resolve([]));
}

function randomString(length) {
    var text = ""
    var possible = "abcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < length; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length))
    }

    return text;
}