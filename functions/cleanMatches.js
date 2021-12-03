'use strict';

// caution: you might want to temporarily delete notifyMatchDeleted function before running this for a large time range

const functions = require('firebase-functions');
const admin = require('firebase-admin');

const firestore = admin.firestore();
const matches = firestore.collection('matches')

const TOKEN = functions.config().quiznerd.token;

function run(req, res) {
    if (req.query.token !== TOKEN) return res.status(401).end();

    const refDate = new Date(new Date().valueOf() - 180 * 24 * 60 * 60 * 1000) // 180 days

    matches
        .where('active', '==', true)
        .where('updated', '<=', refDate)
        .get()
        .then(snapshot => {
            if (snapshot.empty) return
            Promise.all(snapshot.docs.map(d => d.ref.delete()))
                .then(() => {
                    console.log('Deleted ' + snapshot.docs.length + ' documents.')
                    return res.status(200).end()
                })
                .catch((e) => {
                    console.error(e)
                    return res.status(500).end()
                })
        })
}

exports.fn = functions.https.onRequest(run);