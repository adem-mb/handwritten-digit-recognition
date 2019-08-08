import sys
from base64 import b64decode
import io
from tensorflow.keras.models import load_model
from numpy import array
from PIL import Image, ImageFilter, ImageOps
import numpy as np


def decode_image(encoded):
    data = b64decode(encoded)
    img = Image.open(io.BytesIO(data))
    img = img.resize((28, 28), Image.ANTIALIAS)
    img = np.array(img)[..., 2]
    return img


def predict(data):
    return "The drawn digit is: " + str(model.predict_classes(array([data]))[0])

model = load_model("mnist_model.h5") #loading the trained model

while True:
    print("ready")
    sys.stdout.flush()
    line = sys.stdin.readline() #waiting for an image
    img=decode_image(line)
    img=img.reshape(784) #flattening the image
    img=img/255 #normalizing the data
    prediction=predict(img)
    print(prediction) #send predection to java process
    sys.stdout.flush()
