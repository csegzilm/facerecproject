import json
import sys

import numpy as np
from PIL import Image
from io import BytesIO

from deepface import DeepFace
from huggingface_hub import hf_hub_download
from supervision import Detections, BoxAnnotator
import matplotlib.pyplot as plt
from ultralytics import YOLO

print("Python script elindult - sent from python")
#print("Python script elindult", file=sys.stderr)

model_path = hf_hub_download(repo_id="arnabdhar/YOLOv8-Face-Detection", filename="model.pt")
# Modell betöltése
model = YOLO(model_path)

try:
    image_data = sys.stdin.buffer.read()  # Várakozás az összes beérkező byte-ra
    if not image_data:
        raise ValueError("Nem érkezett adat a stdin-ről!")

    image = Image.open(BytesIO(image_data))
    #image.show()  # Csak teszteléshez
except Exception as e:
    print(f"Hiba történt a Python scriptben: {e}", file=sys.stderr)
    sys.exit(1)  # Kilépés hibakóddal

output = model(image)

# Eredmények feldolgozása
results = Detections.from_ultralytics(output[0])

bounding_boxes = results.xyxy.tolist()  # Listává alakítás a returnhöz

# Analizishez:
bounding_boxes_analysis = []
# Kép méretei
w, h = image.size

# Padding arány (pl. 10%)
padding_ratio_x = 0.035
padding_ratio_y = 0.035

# Ebből számoljuk a padding pixelekben
padding_x = int(w * padding_ratio_x)
padding_y = int(h * padding_ratio_y)

for box in bounding_boxes:
    x1, y1, x2, y2 = box

    # Új koordináták számítása a paddinggel, határok ellenőrzése
    x1_new = max(0, int(x1 - padding_x))
    y1_new = max(0, int(y1 - padding_y))
    x2_new = min(w, int(x2 + padding_x))
    y2_new = min(h, int(y2 + padding_y))

    bounding_boxes_analysis.append([x1_new, y1_new, x2_new, y2_new])

# Arcok detektálása és embedding kinyerése
genders = []
emotions = []
races = []

# start_time = time.time()  # script elejére
for box in bounding_boxes_analysis:
    left, top, right, bottom = map(int, box)
    face_image = image.crop((left, top, right, bottom))

    analysis = DeepFace.analyze(np.array(face_image), actions=["gender", "emotion", "race"], detector_backend="skip", silent=True, enforce_detection=False)
    #print(analysis)
    gender = analysis[0]["dominant_gender"]
    emotion = analysis[0]["dominant_emotion"]
    race = analysis[0]["dominant_race"]
    #print(emotion)
    #print(gender)
    #print(race)

    genders.append(gender)
    emotions.append(emotion)
    races.append(race)



# Bounding box adatok JSON formátumba alakítása
json_output = json.dumps({"bounding_boxes": bounding_boxes,
                          "genders": genders,
                          "emotions": emotions,
                          "races": races})
print(json_output)
