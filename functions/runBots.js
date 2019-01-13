'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

const firestore = admin.firestore();
const matches = firestore.collection('matches')
const botdata = firestore.collection('botdata')

const bots = require('./botConfig.json');

const TOKEN = 'HXqNtpzTtcXu';

let allMatches = {};
let allPending = {};
let playQueue = {};

function run(data, context) {
    if (data.token !== TOKEN) return;

    bots.forEach((bot) => {
        fetchMatches(bot)
            .then(fetchPending)
            .then(schedule)
            .then(play);
    });
}

function fetchMatches(bot) {
    return matches
        .where('active', '==', true)
        .where('player2.id', '==', bot.id)
        .get()
        .then(querySnapshot => {
            if (querySnapshot.empty) allMatches[bot.id] = [];
            else allMatches[bot.id] = querySnapshot.docs;
            return bot;
        });
}

function fetchPending(bot) {
    return botdata
        .where('active', '==', true)
        .where('botid', '==', bot.id)
        .get()
        .then(querySnapshot => {
            if (querySnapshot.empty) allPending[bot.id] = [];
            else allPending[bot.id] = querySnapshot.docs;
            return bot;
        })
}

function schedule(bot) {
    let promises = [];

    let pendingIds = allPending[bot.id]
        .map(d => d.data())
        .map(d => d.match.id);

    playQueue[bot.id] = allPending[bot.id]
        .filter(d => d.data().nextExecution <= new Date())
        .filter(d => {
            let match = allMatches[bot.id].find(m => m.id === d.data().match.id);
            return isBotsTurn(match.data());
        });

    promises = promises.concat(
        allMatches[bot.id]
            .filter(d => !pendingIds.includes(d.id))
            .map(d => {
                let timeOffset = randInt(bot.intervalMinutesMin, bot.intervalMinutesMax + 1);

                return botdata.add({
                    active: true,
                    botid: bot.id,
                    match: d.ref,
                    nextExecution: addMinutes(new Date(), timeOffset)
                });
            })
    );

    promises = promises.concat(
        allPending[bot.id]
            .map(d => {
                let matchData = allMatches[bot.id].find(m => m.id === d.data().match.id).data();
                let timeOffset = randInt(bot.intervalMinutesMin, bot.intervalMinutesMax + 1);

                return d.ref.update({
                    active: !((isBotsTurn(matchData) && matchData.round === 4) || (!isBotsTurn && matchData.round === 3)),
                    nextExecution: addMinutes(new Date(), timeOffset)
                })
            })
    );

    return Promise.all(promises).then(() => bot);
}

function play(bot) {
    return Promise.all(
        playQueue[bot.id]
        .map(d => {
            let match = allMatches[bot.id].find(m => m.id === d.data().match.id);
            let matchData = match.data();
            let currentRound = matchData.rounds[matchData.round - 1];
            let myAnswers = currentRound.questions.map(q => {
                let correct = q.answers.find(a => a.correct).id;
                let succeed = Array(100).fill(0).map(() => randInt(0, 2)).reduce((a, b) => a + b, 0) < bot.successRate * 100;
                let answer = succeed
                    ? correct
                    : shuffle(Array(q.answers.length).fill(0).map((x, y) => x + y).filter(a => a !== correct))[0];
                return answer;
            });

            let newRoundData = matchData.rounds;
            newRoundData[currentRound.id - 1].answers2 = myAnswers;
            return match.ref.update({ rounds: newRoundData });
        })
    ).then(() => bot);
}

function addMinutes(date, diff) {
    return new Date(date.getTime() + diff * 60000);
}

function randInt(min, max) {
    return Math.floor(Math.random() * max) + min;
}

function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
}

function isBotsTurn(match) {
    let currentRound = match.rounds[match.round - 1];
    let nMyAnswers = currentRound.answers2.filter(a => a !== -1).length; // bot can never challenge -> is always player2
    let nOpponentAnswers = currentRound.answers1.filter(a => a !== -1).length;
    let nQuestions = currentRound.questions.length;

    if (nMyAnswers === nOpponentAnswers) return true;
    if (nMyAnswers < nQuestions) {
        let hasPlayed = true;
        for (let i = 0; i < currentRound.questions.length; i++) {
            if (currentRound.answers2[i] === -1) hasPlayed = false;
        }
        if (hasPlayed) return true;
        if (nMyAnswers > nOpponentAnswers) return true;
    }
    return false;
}

exports.fn = functions.https.onCall(run);