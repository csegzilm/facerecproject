import sys
from PIL import Image
import io
import struct

# Olvassuk be a kép méretét (4 byte, big-endian int)
#image_size_bytes = sys.stdin.buffer.read(4)
#image_size = struct.unpack('>I', image_size_bytes)[0]

# Olvassuk be a kép adatát
#image_bytes = sys.stdin.buffer.read(image_size)

# Kép betöltése bytefolyamból
#image = Image.open(io.BytesIO(image_bytes))

# Kép információk kiíratása
#print(f"Image received: {image.format}, {image.size}, {image.mode}")

# (Opcionális) Kép megnyitása
#image.show()

# (Opcionális) Feldolgozás vagy mentés
# image.save("received_image.png")

# Visszajelzés a Java felé
#print("Image processing complete!")

# Könyvtárak importálása
from huggingface_hub import hf_hub_download
from ultralytics import YOLO
from supervision import Detections, BoxAnnotator
from PIL import Image
import matplotlib.pyplot as plt
import tkinter as tk
from tkinter import filedialog

# Fájlválasztó ablak megnyitása
root = tk.Tk()
root.withdraw()  # Elrejti a főablakot
image_path = filedialog.askopenfilename(title="Válassz egy képet", filetypes=[("Image files", "*.jpg *.jpeg *.png")])

if not image_path:
    print("Nem választottál képet!")
    exit()

# Modell letöltése
model_path = hf_hub_download(repo_id="arnabdhar/YOLOv8-Face-Detection", filename="model.pt")

# Modell betöltése
model = YOLO(model_path)

image_size_bytes = sys.stdin.buffer.read(4)
image_size = struct.unpack(">I", image_size_bytes)[0]

# Kép adatainak beolvasása
image_bytes = sys.stdin.buffer.read(image_size)
# Kép betöltése és elemzés
#image = Image.open(image_path)
image = Image.open(io.BytesIO(image_bytes))
output = model(image)

# Eredmények feldolgozása
results = Detections.from_ultralytics(output[0])

# Box annotációk rajzolása
annotator = BoxAnnotator()
annotated_image = annotator.annotate(scene=image, detections=results)

# Kép megjelenítése
plt.imshow(annotated_image)
plt.axis('off')  # Kikapcsolja a tengelyeket
plt.show()

