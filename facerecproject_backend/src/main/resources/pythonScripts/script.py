import json
import sys
from PIL import Image
from io import BytesIO
from huggingface_hub import hf_hub_download
from supervision import Detections, BoxAnnotator
import matplotlib.pyplot as plt
from ultralytics import YOLO

print("Python script elindult", file=sys.stderr)

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

# Box annotációk rajzolása
# annotator = BoxAnnotator()
# annotated_image = annotator.annotate(scene=image, detections=results)

# Kép megjelenítése
# plt.imshow(annotated_image)
# plt.axis('off')  # Kikapcsolja a tengelyeket
# plt.show()

# Bounding box adatok JSON formátumba alakítása
bounding_boxes = results.xyxy.tolist()  # Listává alakítás
json_output = json.dumps({"bounding_boxes": bounding_boxes})
print(json_output)
