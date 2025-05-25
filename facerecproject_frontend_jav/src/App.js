// import React from "react";
// import { BrowserRouter as Router, Route, Routes, Link } from "react-router-dom";
// import FileUpload from "./FileUpload";
// import FormPage from "./AdvertFormPage";
// import './App.css';

// function App() {
//     return (
//         <div className="App">
//             <header className="App-header">
//                 <h1>Arcfelismerő alkalmazás</h1>
//                 <FileUpload />
//             </header>
//         </div>
//     );
// }

// export default App;

import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Route, Routes, Link } from "react-router-dom";
import FileUpload from "./FileUpload";
import FormPage from "./AdvertFormPage";
import AdvertDisplayPage from "./AdvertDisplayPage";  // Az új űrlap oldal importálása

import './App.css';

function App() {
    const [realTimeData, setRealTimeData] = useState(null); // Állapot tárolása a szülőben

    useEffect(() => {
        if (realTimeData) {
            console.log("Frissített realTimeData:", realTimeData);  // Logoljuk a frissített adatokat
        }
    }, [realTimeData]);  // Figyeljük, amikor a realTimeData változik

    // return (
    //     <Router>
    //         <div className="App">
    //             <header className="App-header">
    //                 <h1>Arcfelismerő alkalmazás</h1>
    //                 <nav>
    //                     <ul>
    //                         <li>
    //                             <Link to="/" className="nav-link">File Upload</Link>
    //                         </li>
    //                         <li>
    //                             <Link to="/form" className="nav-link">Hirdetés hozzáadása</Link>
    //                         </li>
    //                         <li>
    //                             <Link to="/addisplay" className="nav-link">Aktuális hirdetés</Link>
    //                         </li>
    //                     </ul>
    //                 </nav>
    //                 <Routes>
    //                     <Route path="/" element={<FileUpload setRealTimeData={setRealTimeData} />} />
    //                     <Route path="/form" element={<FormPage />} />
    //                     <Route path="/addisplay" element={<AdvertDisplayPage realTimeData={realTimeData} />} />
    //                 </Routes>
    //             </header>
    //         </div>
    //     </Router>
    // );

    return (
        <Router>
            <div className="App">
                <header className="App-header">
                    <h1>Arcfelismerő alkalmazás</h1>
                    <div className="content">
                        {/* FileUpload és AdvertDisplayPage egymás mellett */}
                        <div className="file-upload-container">
                            <FileUpload setRealTimeData={setRealTimeData} />
                        </div>
                        <div className="advert-container">
                            <AdvertDisplayPage realTimeData={realTimeData} />
                        </div>
                        <div className="advert-container">
                            <FormPage />
                        </div>
                    </div>
                    {/* <Routes>
                        <Route path="/form" element={<FormPage />} />
                    </Routes> */}
                </header>
            </div>
        </Router>
    );
}

export default App;

