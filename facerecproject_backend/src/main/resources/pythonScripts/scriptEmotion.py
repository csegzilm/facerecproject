import asyncio
import websockets
import base64
import json
from PIL import Image
from io import BytesIO
from huggingface_hub import hf_hub_download
from ultralytics import YOLO
from supervision import Detections
from deepface import DeepFace
import numpy as np

# ws://localhost:8767-es

print("Python WebSocket szerver indul...")

# YOLO modell betöltése
model_path = hf_hub_download(repo_id="arnabdhar/YOLOv8-Face-Detection", filename="model.pt")
model = YOLO(model_path)


async def process_image(image):
    try:
       # YOLO detekció
        results = model(image)[0]
        detections = Detections.from_ultralytics(results)

        # Arcok koordinátái
        bounding_boxes = detections.xyxy.tolist()
        genders, emotions, ages = [], [], []

        for box in bounding_boxes:
            x1, y1, x2, y2 = map(int, box)
            face = image.crop((x1, y1, x2, y2)).resize((224, 224))

            analysis = DeepFace.analyze(
                np.array(face),
                actions=["emotion"],
                detector_backend="skip",
                enforce_detection=False,
                silent=True
            )[0]

            emotions.append(analysis["dominant_emotion"])

        return json.dumps({
            "bounding_boxes": bounding_boxes,
            "genders": genders,
            "emotions": emotions,
            "ages": ages
        })

    except Exception as e:
        print(f"Hiba képfeldolgozás közben: {e}")
        return json.dumps({
            "bounding_boxes": [],
            "genders": [],
            "emotions": [],
            "ages": [],
            "error": str(e)
        })


async def handler(websocket):
    print("Java backend csatlakozott.")
    try:
        async for message in websocket:
            if isinstance(message, bytes):  # Bináris adat
                image = Image.open(BytesIO(message)).convert("RGB")
                result = await process_image(image)
                await websocket.send(result)
            else:
                print("Nem bináris üzenet érkezett.")
    except websockets.exceptions.ConnectionClosed:
        print("Kapcsolat megszakadt.")


async def main():
    server = await websockets.serve(handler, "localhost", 8767)
    print("Python WebSocket szerver fut a ws://localhost:8767 címen")
    await asyncio.Future()  # végtelen futás


if __name__ == "__main__":
    asyncio.run(main())
