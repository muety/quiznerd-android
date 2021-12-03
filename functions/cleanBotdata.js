'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

const firestore = admin.firestore();
const botdata = firestore.collection('botdata')

const TOKEN = functions.config().quiznerd.token;

function run(req, res) {
    if (req.query.token !== TOKEN) return res.status(401).end();

    let totalCount = 0

    const p1 = botdata.where('active', '==', false).get()
        .then(snapshot => {
            if (snapshot.empty) return
            totalCount += snapshot.docs.length
            return Promise.all(snapshot.docs.map(d => d.ref.delete()))
        })

    const p2 = botdata.where('nextExecution', '<', new Date()).get()
        .then(snapshot => {
            if (snapshot.empty) return
            totalCount += snapshot.docs.length
            return Promise.all(snapshot.docs.map(d => d.ref.delete()))
        })

    Promise.all([p1, p2])
        .then(() => {
            console.log('Deleted ' + totalCount + ' documents.')
            return res.status(200).end()
        })
        .catch((e) => {
            console.error(e)
            return res.status(500).end()
        })
}

exports.fn = functions.https.onRequest(run);