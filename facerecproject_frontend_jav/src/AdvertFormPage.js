import React, { useState } from "react";
import './AdvertFormPage.css';


const AdvertFormPage = () => {
    const [name, setName] = useState("");
    const [minPeople, setMinPeople] = useState(""); // a minimum, amennyi embernek megjelenítjük 
    const [prefEmotion, setPrefEmotion] = useState(""); // Preferred emotion, preferált érzelem
    const [prefGender, setPrefGender] = useState(""); // Preferred gender, preferált nem
    const [prefAgeGroup, setPrefAgeGroup] = useState(""); // Preferred age group, preferált korcsoport
    const [price, setPrice] = useState(""); //Amennyit fizet a hírdetésfeladó
    const [image, setImage] = useState(null); //A hirdetés képe (url)

    const handleFormSubmit = (e) => {
        e.preventDefault();

        // Az adatokat a web böngésző storage-ba mentjük
        const formData = {
            name,
            minPeople,
            prefEmotion,
            prefGender,
            prefAgeGroup,
            price,
            image: image ? URL.createObjectURL(image) : null, // Az image URL-jét mentjük el
        };

        localStorage.setItem(name, JSON.stringify(formData)); // Böngésző localstorage-ába mentés

        alert("Adatok mentve!");
    };

    const handleImageUpload = (e) => {
        setImage(e.target.files[0]);
    };

    return (
        <div style={{ padding: "20px" }}>
            <h1>Hírdetés hozzáadása</h1>
            <form onSubmit={handleFormSubmit}>
                <div className="form-row">
                    <label>Név:</label>
                    <input
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                </div>
                <div className="form-row">
                    <label>Preferált Minimum számú ember:</label>
                    <input
                        type="number"
                        value={minPeople}
                        onChange={(e) => setMinPeople(e.target.value)}
                        required
                    />
                </div>
                <div className="form-row">
                    <label>Preferált érzelem:</label>
                    <select
                        value={prefEmotion}
                        onChange={(e) => setPrefEmotion(e.target.value)}
                    >
                        <option value="">Válassz</option>
                        <option value="happy">Boldog</option>
                        <option value="sad">Szomorú</option>
                        <option value="fear">Fél</option>
                        <option value="neutral">Neutrális</option>
                    </select>
                </div>
                <div className="form-row">
                    <label>Preferált nem:</label>
                    <select
                        value={prefGender}
                        onChange={(e) => setPrefGender(e.target.value)}
                    >
                        <option value="">Válassz</option>
                        <option value="Woman">Nő</option>
                        <option value="Man">Férfi</option>
                    </select>
                </div>
                <div className="form-row">
                    <label>Preferált korcsoport:</label>
                    <select
                        value={prefGender}
                        onChange={(e) => setPrefAgeGroup(e.target.value)}
                    >
                        <option value="">Válassz</option>
                        <option value="18-30">18-30</option>
                        <option value="31-40">31-40</option>
                        <option value="41-50">41-50</option>
                        <option value="51-60">51-60</option>
                        <option value="60+">60+</option>
                    </select>
                </div>
                <div className="form-row">
                    <label>Fizetett ár:</label>
                    <input
                        type="number"
                        value={price}
                        onChange={(e) => setPrice(e.target.value)}
                        required
                    />
                </div>
                <div className="form-row">
                    <label>Hirdetési kép feltöltése:</label>
                    <input type="file" accept="image/*" onChange={handleImageUpload} />
                </div>
                <button type="submit">Feltölt</button>
            </form>
        </div>
    );
};

export default AdvertFormPage;
