import numpy as np
import matplotlib.pyplot as plt
from tensorflow.keras.datasets import mnist
from tensorflow.keras.models import Sequential, save_model, load_model
from tensorflow.keras.layers import Dense, Dropout, Flatten, Lambda
from keras.utils import np_utils


def fit():
    (trains, targets_train), (tests, targets_test) = mnist.load_data()

    trains = trains.reshape(60000, 784) #reshpae the 28*28 matrices(images) to 784 vectors
    tests = tests.reshape(10000, 784)

    trains = trains.astype('float32') #using the vectors as real numbers
    tests = tests.astype('float32')

    trains /= 255 #normalizing the vectors
    tests /= 255
    targets_train = np_utils.to_categorical(targets_train, 10) #transform the labels to one-hot encoding
    targets_test = np_utils.to_categorical(targets_test, 10)

    model = Sequential()
    model.add(Flatten(input_shape=(784, ))) #input layer with 784 neurons
    model.add(Dense(280, activation="relu")) #hidden layer with 280 neurons and using ReLU as antication function
    model.add(Dropout(0.25)) #dropout with 25%
    model.add(Dense(260, activation="relu"))
    model.add(Dropout(0.25))
    model.add(Dense(10, activation="softmax")) #output layer with softmax activation function
    model.compile(
        loss="categorical_crossentropy",
        optimizer='adam',
        metrics=["accuracy"]
    )
    history = model.fit(trains, targets_train, verbose=2, epochs=25,  validation_data=(tests, targets_test), batch_size=50)
    model.save("mnist_model.h5")
    return history

history = fit() #start the training
loss_curve = history.history["loss"]
acc_curve = history.history["acc"]
loss_val_curve = history.history["val_loss"]
acc_val_curve = history.history["val_acc"]

plt.plot(loss_curve, label="Train")
plt.plot(loss_val_curve, label="Val")
plt.title("Loss")
plt.legend(loc='best')
plt.show()

plt.plot(acc_curve, label="Train")
plt.plot(acc_val_curve, label="Val")
plt.title("Accuracy")
plt.legend(loc='best')
plt.savefig('accuracy.png')
plt.show()



