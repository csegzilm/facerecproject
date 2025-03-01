import React, { useState, useRef, useEffect, useCallback } from "react";
import axios from "axios";
import { useDropzone } from "react-dropzone";

const FileUpload = () => {
    const [image, setImage] = useState(null);
    const [message, setMessage] = useState("");
    const [facesCoordinates, setFacesCoordinates] = useState([]);
    const [isStreaming, setIsStreaming] = useState(false);
    const videoRef = useRef(null);
    const canvasRef = useRef(null);
    const animationFrameId1 = useRef(null);  // Hozzáadva a requestAnimationFrame ID tárolására
    const animationFrameId2 = useRef(null);  // Hozzáadva a requestAnimationFrame ID tárolására


    const handleResponse = async(formData) => {
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
        });
    
        animationFrameId1.current = requestAnimationFrame(drawFaces); //Az ID eltárolása
    }, [facesCoordinates]);

    

    const detectFacesLive = useCallback(async () => {
        if (!isStreaming) return;
    
        try {
            const frame = await captureFrame(); // Várjuk meg a blobot
            const formData = new FormData();
            formData.append("file", new File([frame], "frame.png", { type: "image/png" }));
    
            await handleResponse(formData); // Feltöltés a backendre
    
        } catch (error) {
            console.error("Error capturing frame:", error);
        }
    
        //drawFaces();
        animationFrameId2.current = requestAnimationFrame(detectFacesLive); // Következő képkocka feldolgozása
    }, [isStreaming]);
    

    const startCamera = useCallback(async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ video: true });
            if (videoRef.current && !videoRef.current?.srcObject) { // Ellenőrizd, hogy a ref már létezik-e
                videoRef.current.srcObject = stream;
                await videoRef.current.play();
                detectFacesLive(); // Arcok felismerésének elindítása
            }
        } catch (error) {
            console.error("Error accessing webcam:", error);
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
            drawFaces();
        } else {
            stopCamera();
        }
    }, [drawFaces, isStreaming, startCamera, stopCamera]);

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
                    style={{
                        maxWidth: "100%",
                        display: isStreaming ? "block" : "none",
                    }}
                    autoPlay
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
                }}>
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
                        />
                    ))}
                </div>
            )}
    
            {/* Kép elemzésének gombja */}
            <button onClick={uploadFile} disabled={!image}>
                Analyze
            </button>
    
            {/* Üzenetek megjelenítése */}
            <p>{message}</p>
        </div>
    );
    






};

export default FileUpload;
