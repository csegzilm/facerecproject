import React, { useState, useRef, useEffect, useCallback } from "react";
import axios from "axios";
import { useDropzone } from "react-dropzone";

const FileUpload = () => {
    const [image, setImage] = useState(null);
    const [message, setMessage] = useState("");
    const [facesCoordinates, setFacesCoordinates] = useState([]);
    const [isStreaming, setIsStreaming] = useState(false);
    const [socket, setSocket] = useState(null);
    const videoRef = useRef(null);
    const canvasRef = useRef(null);
    const animationFrameId1 = useRef(null);  // Hozzáadva a requestAnimationFrame ID tárolására
    const animationFrameId2 = useRef(null);  // Hozzáadva a requestAnimationFrame ID tárolására
    const timeoutId = useRef(null);

    const [emotionResults, setEmotionResults] = useState([]); // sorrend: happyCount, sadCount, fearCount, neutralCount
    const [ageResults, setAgeResults] = useState(0);
    const [genderResults, setGenderResults] = useState([]);



    useEffect(() => {
        // WebSocket kapcsolat létrehozása
        const ws = new WebSocket('ws://localhost:8080/ws'); // A backend URL-je

        // WebSocket események
        ws.onopen = () => {
            console.log("WebSocket kapcsolat létrejött (frontend).");
        };

        ws.onclose = (event) => {
            console.log("WebSocket kapcsolat lezárult: ", event);
        };

        ws.onerror = (error) => {
            console.error("WebSocket hiba:", error);
            if (error.message) {
                console.error("Hibaüzenet:", error.message);
            }
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                console.log("Kapott üzenet:", data);
                if (data.bounding_boxes && Array.isArray(data.bounding_boxes)) {
                    const coordinates = data.bounding_boxes.map((box, idx) => ({
                        x: box[0],
                        y: box[1],
                        width: box[2] - box[0],
                        height: box[3] - box[1],
                        gender: data.genders?.[idx],
                        emotion: data.emotions?.[idx],
                        age: data.ages?.[idx]
                    }));

                    //Új rész
                    if (data.genders && data.genders.length > 0) {
                        // van legalább egy gender
                        setGenderResults(evaluateGender(data.genders));
                    }
                    if (data.emotions && data.emotions.length > 0) {
                        // van legalább egy emotion
                        setEmotionResults(evaluateEmotion(data.emotions));
                    }
                    if (data.ages && data.ages.length > 0) {
                        // van legalább egy age
                        setAgeResults(evaluateAge(data.ages));
                    }

                    setFacesCoordinates(coordinates);
                }
            } catch (e) {
                console.error("Hiba a JSON feldolgozása közben:", e);
            }
        };

        // window.onbeforeunload: kapcsolat lezárása frissítés előtt
        window.onbeforeunload = () => {
            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.close(); // A kapcsolat lezárása
            }
        };

        setSocket(ws); //Így a socket típusa WebSocket lesz

        return () => {
            ws.close();
        };
    }, []);

    const handleResponse = async (formData) => {
        try {
            const response = await axios.post("http://localhost:8080/api/images/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" },
            });

            setMessage(`${response.data.message}, Number of faces:  ${response.data.faceCount}`);
            console.log(response.data.facesCoordinates);

            //const proba = response.data.facesCoordinates;
            //console.log(proba);
            //setFacesCoordinates(facesCoordinates);
            //console.log(facesCoordinates);
            const isValid = response.data.facesCoordinates.every(face => {
                // Minden face objektumnál ellenőrizzük, hogy léteznek és érvényesek a koordináták
                return (
                    typeof face.x === 'number' &&
                    typeof face.y === 'number' &&
                    typeof face.width === 'number' &&
                    typeof face.height === 'number' &&
                    face.x >= 0 && face.y >= 0 && face.width > 0 && face.height > 0
                );
            });

            if (isValid) {
                setFacesCoordinates(response.data.facesCoordinates);
            } else {
                setMessage("Invalid face coordinates detected.");
            }

        } catch (error) {
            setMessage("Hiba történt a feltöltéskor");
        }
    };

    const drawFaces = useCallback(() => {
        const canvas = canvasRef.current;
        const context = canvas?.getContext("2d");
        const video = videoRef.current;

        if (!canvas || !context || !video) return;

        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        context.clearRect(0, 0, canvas.width, canvas.height);

        facesCoordinates.forEach((face) => {
            context.strokeStyle = "red";
            context.lineWidth = 2;
            context.strokeRect(face.x, face.y, face.width, face.height);

            // Megjelenítjük az arc attribútumokat (gender, emotion, race) is
            const infoText = `${face.gender || "Unknown Gender"} | ${face.emotion || "Unknown Emotion"} | ${face.race || "Unknown Race"}`;
            context.fillStyle = "white";
            context.font = "8px Arial";
            context.fillText(infoText, face.x, face.y - 10); // A szöveget az arc felett jelenítjük meg
        });

        animationFrameId1.current = requestAnimationFrame(drawFaces); //Az ID eltárolása
    }, [facesCoordinates]);



    // const detectFacesLive = useCallback(async () => {
    //     if (!isStreaming || !socket) return;

    //     try {
    //         const frame = await captureFrame(); // Várjuk meg a blobot
    //         const formData = new FormData();
    //         formData.append("file", new File([frame], "frame.png", { type: "image/png" }));

    //         await handleResponse(formData); // Feltöltés a backendre

    //     } catch (error) {
    //         console.error("Error capturing frame:", error);
    //     }

    //     //drawFaces();
    //     animationFrameId2.current = requestAnimationFrame(detectFacesLive); // Következő képkocka feldolgozása
    // }, [isStreaming]);

    const detectFacesLive = useCallback(() => {
        if (!isStreaming || !socket || socket.readyState !== WebSocket.OPEN) {
            if (animationFrameId2.current) {
                cancelAnimationFrame(animationFrameId2.current);
                animationFrameId2.current = null;
            }
            return;
        }

        const canvas = canvasRef.current;
        const video = videoRef.current;
        const context = canvas.getContext("2d");

        // Átméretezés a videó aktuális méretére
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        // Kirajzoljuk a kameraképet a canvasra
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Async módon elküldjük a képet, de nem blokkoljuk a rajzolást!
        canvas.toBlob(blob => {
            if (socket.readyState === WebSocket.OPEN && blob) {
                socket.send(blob); // Blobot küldünk közvetlenül
            }
        }, "image/jpeg", 0.9); // JPEG 90% tömörítéssel

        // Azonnal kérjük a következő frame-et!
        animationFrameId2.current = requestAnimationFrame(detectFacesLive);
    }, [isStreaming, socket]);



    // const startCamera = useCallback(async () => {
    //     try {
    //         const stream = await navigator.mediaDevices.getUserMedia({ video: true });
    //         if (videoRef.current && !videoRef.current?.srcObject) { // Ellenőrizzük, hogy a ref már létezik-e
    //             videoRef.current.srcObject = stream;
    //             await videoRef.current.play();
    //             detectFacesLive(); // Arcok felismerésének elindítása
    //         }
    //     } catch (error) {
    //         console.error("Error accessing webcam:", error);
    //     }
    // }, [detectFacesLive]);

    // VIdeós verzió
    const startCamera = useCallback(async () => {
        try {
            if (videoRef.current) {
                await videoRef.current.play(); // csak elindítjuk a videót
                detectFacesLive(); // folytatódik a feldolgozás
            }
        } catch (error) {
            console.error("Error playing video:", error);
        }
    }, [detectFacesLive]);


    const stopCamera = useCallback(() => {
        const stream = videoRef.current?.srcObject;
        if (stream) {
            const tracks = stream.getTracks();
            tracks.forEach(track => track.stop());
            videoRef.current.srcObject = null; // Eltávolítja a forrást, hogy ne maradjon fekete képernyő
        }

        if (animationFrameId1.current) {
            cancelAnimationFrame(animationFrameId1.current);
            animationFrameId1.current = null;  // Reset ID
        }

        if (animationFrameId2.current) {
            cancelAnimationFrame(animationFrameId2.current);
            animationFrameId2.current = null;  // Reset ID
        }

        if (timeoutId.current) { // ÚJ!
            clearTimeout(timeoutId.current);
            timeoutId.current = null;
        }

        //Canvas törlése, hogy ne maradjon ott az utolsó frame
        if (canvasRef.current) {
            const context = canvasRef.current.getContext('2d');
            context.clearRect(0, 0, canvasRef.current.width, canvasRef.current.height);
        }
    }, [videoRef]);

    const { getRootProps, getInputProps } = useDropzone({
        accept: "image/*",
        onDrop: (acceptedFiles) => {
            setImage(acceptedFiles[0]);
            setFacesCoordinates([]);
            setMessage("");
            setIsStreaming(false);
        },
    });

    const captureFrame = () => {
        const canvas = canvasRef.current;
        const video = videoRef.current;
        const context = canvas.getContext("2d");

        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        return new Promise((resolve) => {
            canvas.toBlob(blob => resolve(blob), "image/png");
        });

    }

    const evaluateEmotion = (emotions) => {
        let happyCount = 0;
        let sadCount = 0;
        let fearCount = 0;
        let neutralCount = 0;

        for (const element of emotions) {
            if (element === "happy") happyCount++;
            else if (element === "sad") sadCount++;
            else if (element === "fear") fearCount++;
            else if (element === "neutral") neutralCount++;
        }

        return [happyCount, sadCount, fearCount, neutralCount];
    }

    const evaluateAge = (ages) => {
        const numbers = ages.map(Number); //Stringből intté
        const sum = numbers.reduce((acc, val) => acc + val, 0);
        const average = sum / numbers.length;

        return average;
    }

    const evaluateGender = (genders) => {
        let manCount = 0;
        let womanCount = 0;

        for (const element of genders) {
            if (element === "Man") manCount++;
            else if (element === "Woman") womanCount++;
        }

        return [manCount, womanCount];
    }

    const uploadFile = async () => {
        if (!image) return;

        const formData = new FormData();
        formData.append("file", image);

        handleResponse(formData);
    };

    useEffect(() => {
        if (isStreaming) {
            setImage(null);
            startCamera();
            //drawFaces();
        } else {
            stopCamera();
        }
    }, [/*drawFaces,*/ isStreaming, startCamera, stopCamera]);

    return (
        <div>
            {/* Kép feltöltési terület */}
            <div {...getRootProps()} style={{ border: "2px dashed gray", padding: "10px", cursor: "pointer" }}>
                <input {...getInputProps()} />
                {image ? <p>{image.name}</p> : <p>Húzd ide a képet vagy kattints a feltöltéshez</p>}
            </div>

            {/* Kamera és vászon a valós idejű arcfelismeréshez */}
            <div style={{ position: "relative", maxWidth: "100%" }}>
                <video
                    ref={videoRef}
                    src="/crowd1.mp4" // Új idiglenes sor: a videó fájl elérési útja
                    style={{
                        maxWidth: "100%",
                        display: isStreaming ? "block" : "none",
                    }}
                    autoPlay
                    loop
                    muted
                ></video>
                <canvas
                    ref={canvasRef}
                    style={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        maxWidth: "100%",
                        pointerEvents: "none"
                    }}
                />
            </div>

            {/* Kamera vezérlő gomb */}
            <button onClick={() => {
                setIsStreaming((prev) => !prev);
                setFacesCoordinates([]);
                setMessage("");
            }}

                style={{
                    padding: "10px 20px",
                    fontSize: "16px",
                    backgroundColor: isStreaming ? "#ff4d4d" : "#4caf50",
                    color: "white",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer",
                    margin: "10px",
                    transition: "background-color 0.3s"
                }}
            >
                {isStreaming ? "Stop" : "Start"} Camera
            </button>

            {/* Feltöltött kép és az arcfelismerés eredményei */}
            {image && (
                <div style={{ position: "relative", display: "inline-block", maxWidth: "100%" }}>
                    <img
                        src={URL.createObjectURL(image)}
                        alt="Uploaded"
                        style={{ display: "block", maxWidth: "100%" }}
                    />
                    {facesCoordinates.map((face, index) => (
                        <div
                            key={index}
                            style={{
                                position: "absolute",
                                top: face.y,
                                left: face.x,
                                width: face.width,
                                height: face.height,
                                border: "2px solid red",
                                boxSizing: "border-box",
                            }}
                        >
                            {/* Megjelenítjük az arc attribútumokat a képen */}
                            <div
                                style={{
                                    position: "relative",
                                    top: -20,
                                    left: 0,
                                    width: 100,
                                    backgroundColor: "rgba(0, 0, 0, 0.7)",
                                    color: "white",
                                    fontSize: "9px",
                                    padding: "2px 5px",
                                    borderRadius: "5px",
                                }}
                            >
                                {`${face.gender || "Unknown"}, ${face.emotion || "Unknown"}, ${face.race || "Unknown"}`}
                            </div>
                        </div>
                    ))}

                </div>
            )}

            {/* Kép elemzésének gombja */}
            <button onClick={uploadFile} disabled={!image}
                style={{
                    padding: "10px 20px",
                    fontSize: "16px",
                    backgroundColor: image ? "#0077cc" : "#cccccc",
                    color: "white",
                    border: "none",
                    borderRadius: "5px",
                    cursor: image ? "pointer" : "not-allowed",
                    margin: "10px",
                    transition: "background-color 0.3s"
                }}
            >
                Analyze
            </button>

            {/* Üzenetek megjelenítése */}
            <p>{message}</p>
            <p>Átlag kor: {ageResults}</p>
            <p>Férfiak száma: {genderResults[0]}, Nők száma: {genderResults[1]}</p>
            <p>Érzelmek: {emotionResults[0]} boldog, {emotionResults[1]} szomorú, {emotionResults[2]} fél, {emotionResults[3]} neutrális</p>


        </div>
    );
};

export default FileUpload;
