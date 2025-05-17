import React, { useState, useEffect } from 'react';
import './AdvertDisplayPage.css'; // A CSS fájl importálása

// // Hirdetéshez rendelhető paraméterek és súlyok
// const ad = {
//     emotion: "happy",  // preferált érzelem
//     minPeople: 5,      // minimum számú ember
//     gender: "male",    // preferált gender
//     price: 5000,       // hirdetés ára
//     emotionWeight: 0.4,
//     peopleWeight: 0.2,
//     genderWeight: 0.2,
//     priceWeight: 0.2
// };

// // Valós idejű adatok (csak érzelmek számát tartalmazza)
// const realTimeData = {
//     happyCount: 4,    // boldog emberek száma
//     sadCount: 1,      // szomorú emberek száma
//     fearCount: 0,     // félelem érző emberek száma
//     neutralCount: 1,  // neutrális emberek száma
//     peopleCount: 6,   // jelenlévők összes száma
//     maleCount: 4,     // férfiak száma
//     femaleCount: 2,   // nők száma
//     averageAge: 25,   // átlagos életkor
//     minAge: 18,       // minimum életkor
//     maxAge: 30,       // maximum életkor
// };

// // Kiszámítja az érzelem egyezést és az érzelem súlyozását
// function calculateEmotionMatch(adEmotion, realTimeData) {
//     // Összesen hányan vannak, és hogyan oszlanak el az érzelmek
//     const totalPeople = realTimeData.happyCount + realTimeData.sadCount + realTimeData.fearCount + realTimeData.neutralCount;

//     // Az érzelmekhez rendelt súlyok
//     const emotionWeights = {
//         happy: 0.7,    // Pozitív érzelem - boldog
//         neutral: 0.6,  // Pozitív érzelem - neutrális
//         sad: 0.3,      // Negatív érzelem - szomorú
//         fear: 0.3      // Negatív érzelem - félelem
//     };

//     // Az aktuális érzelemhez tartozó súly számítása
//     const emotionWeight = emotionWeights[adEmotion] || 0.5;  // Ha nem ismert érzelem, alapértelmezett súly

//     // Az egyezés kiszámítása: hány ember tartozik a preferált érzelemhez
//     const emotionCount = realTimeData[`${adEmotion}Count`] || 0;
//     const emotionMatch = emotionCount / totalPeople;

//     return emotionMatch * emotionWeight;
// }

// // Kiszámítja a gender egyezést
// function calculateGenderMatch(adGender, realMaleCount, realFemaleCount) {
//     const totalPeople = realMaleCount + realFemaleCount;
//     const realGenderRatio = realMaleCount / totalPeople;

//     return adGender === "male" ? realGenderRatio : (1 - realGenderRatio);
// }

// // Kiszámítja a kor egyezést
// function calculateAgeMatch(adMinAge, adMaxAge, realAge) {
//     if (realAge >= adMinAge && realAge <= adMaxAge) {
//         return 1;  // Teljes egyezés
//     }
//     return 0;  // Nincs egyezés
// }

// // Kiszámítja a minimum számú ember egyezést
// function calculateMinPeopleMatch(adMinPeople, realPeopleCount) {
//     return realPeopleCount >= adMinPeople ? 1 : 0;
// }

// // Kiszámítja az ár egyezést
// function calculatePriceMatch(adPrice, realPrice) {
//     return adPrice === realPrice ? 1 : 0;
// }

// // Számítsuk ki az összesített egyezést a különböző paraméterek és súlyok alapján
// function calculateTotalMatch(ad, realTimeData) {
//     const emotionMatch = calculateEmotionMatch(ad.emotion, realTimeData) * ad.emotionWeight;
//     const genderMatch = calculateGenderMatch(ad.gender, realTimeData.maleCount, realTimeData.femaleCount) * ad.genderWeight;
//     const ageMatch = calculateAgeMatch(ad.minAge, ad.maxAge, realTimeData.averageAge) * ad.peopleWeight;
//     const minPeopleMatch = calculateMinPeopleMatch(ad.minPeople, realTimeData.peopleCount) * ad.peopleWeight;
//     const priceMatch = calculatePriceMatch(ad.price, realTimeData.price) * ad.priceWeight;

//     const totalMatch = emotionMatch + genderMatch + ageMatch + minPeopleMatch + priceMatch;
//     return totalMatch;
// }

// // Hirdetés és valós idejű adatok alapján számoljuk ki az egyezést
// const totalMatch = calculateTotalMatch(ad, realTimeData);

// // Kiíratjuk a végső egyezést
// console.log("Összesített egyezés: " + totalMatch.toFixed(2));

// // Döntés, hogy melyik hirdetés a relevánsabb
// if (totalMatch >= 0.8) {
//     console.log("Ez a hirdetés magas relevanciával jelenik meg.");
// } else {
//     console.log("Ez a hirdetés nem annyira releváns.");
// }



const AdvertDisplayPage = ({ realTimeData }) => {
    const [bestAd, setBestAd] = useState(null);
    const peopleWeight = 0.2;
    const emotionWeight = 0.4;
    const genderWeight = 0.2;
    const priceWeight = 0.2;

    // Kiszámítja a relevanciát egy hirdetéshez
    function calculateEmotionMatch(adEmotion, realTimeData) {
        const totalPeople = realTimeData.peopleCount;
        const emotionWeights = {
            happy: 0.7,
            neutral: 0.6,
            sad: 0.3,
            fear: 0.3
        };

        const emotionWeight = emotionWeights[adEmotion] || 0.5;
        const emotionCount = realTimeData[`${adEmotion}Count`] || 0;
        const emotionMatch = emotionCount / totalPeople;

        return emotionMatch * emotionWeight;
    }

    function calculateGenderMatch(adGender, realMaleCount, realFemaleCount) {
        const totalPeople = realMaleCount + realFemaleCount;
        const realGenderRatio = realMaleCount / totalPeople;

        return adGender === "Man" ? realGenderRatio : (1 - realGenderRatio);
    }

    function calculateAgeMatch(adMinAge, adMaxAge, realAge) {
        if (realAge >= adMinAge && realAge <= adMaxAge) {
            return 1;
        }
        return 0;
    }

    function calculateMinPeopleMatch(adMinPeople, realPeopleCount) {
        return realPeopleCount >= adMinPeople ? 1 : 0;
    }

    function calculatePriceMatch(adPrice, realPrice) {
        return adPrice === realPrice ? 1 : 0;
    }

    function calculateTotalMatch(ad, realTimeData) {
        const emotionMatch = calculateEmotionMatch(ad.prefEmotion, realTimeData) * emotionWeight;
        const genderMatch = calculateGenderMatch(ad.prefGender, realTimeData.maleCount, realTimeData.femaleCount) * genderWeight;
        const ageMatch = 0; //calculateAgeMatch(ad.prefAgeGroup, realTimeData.averageAge) * peopleWeight;
        const minPeopleMatch = calculateMinPeopleMatch(ad.minPeople, realTimeData.peopleCount) * peopleWeight;
        const priceMatch = 0; // calculatePriceMatch(ad.price, realTimeData.price) * ad.priceWeight;

        const totalMatch = emotionMatch + genderMatch + ageMatch + minPeopleMatch + priceMatch;
        return totalMatch;
    }

    // Iterálás a localStorage elemein
    function getBestAd() {
        let bestAd = null;
        let highestRelevance = -1;

        // Iterálunk az összes localStorage kulcs-érték páron
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            const adData = JSON.parse(localStorage.getItem(key)); 

            // Kiszámoljuk a relevanciát
            const relevance = calculateTotalMatch(adData, realTimeData);

            // Ha az aktuális relevance magasabb, mint a legnagyobb eddigi, frissítjük
            if (relevance > highestRelevance) {
                highestRelevance = relevance;
                bestAd = adData;
            }
        }

        return bestAd;
    }

    useEffect(() => {
        if (realTimeData) {
            const bestAd = getBestAd();
            setBestAd(bestAd);
        }
    }, [realTimeData]); // Csak akkor frissítjük, amikor a realTimeData változik


    return (
        <div className="image-container">
            <div className="text-container">
                <h1>Mutatott reklám</h1>
            </div>
            <div>
                <h3>Real-Time Data:</h3>
                {realTimeData ? (
                    <pre>{JSON.stringify(realTimeData, null, 2)}</pre>
                ) : (
                    <p>No real-time data available.</p>
                )}
            </div>


            {bestAd ? (
                <div>
                    <h3>Legrelevánsabb hirdetés:</h3>
                    <pre>{JSON.stringify(bestAd, null, 2)}</pre>
                    {bestAd.image ? (
                        <img
                            src={bestAd.image}
                            alt="Ad"
                            style={{ width: "100%", height: "auto", borderRadius: "8px", marginTop: "20px" }}
                        />
                    ) : (
                        <p>Nincs elérhető kép a hirdetéshez.</p>
                    )}
                </div>
            ) : (
                <p>Nincs releváns hirdetés.</p>
            )}

        </div>
    );
}

export default AdvertDisplayPage;