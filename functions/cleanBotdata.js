'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

const firestore = admin.firestore();
const botdata = firestore.collection('botdata')

const TOKEN = functions.config().quiznerd.token;

function run(req, res) {
    if (req.query.token !== TOKEN) return res.status(401).end();

    let totalCount = 0

    Promise.resolve()
        .then(() => botdata.where('active', '==', false).get())
        .then(snapshot => {
            if (snapshot.empty) return
            totalCount += snapshot.docs.length
            return Promise.all(snapshot.docs.map(d => d.ref.delete()))
        })
        // delete everything pending for longer than 24 hours, assuming that the bot script runs at least once every day
        .then(() => botdata.where('nextExecution', '<', new Date(new Date().valueOf() - 24 * 60 * 60 * 1000)).get())
        .then(snapshot => {
            if (snapshot.empty) return
            totalCount += snapshot.docs.length
            return Promise.all(snapshot.docs.map(d => d.ref.delete()))
        })
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