#!/usr/bin/env python
# coding: utf-8

# In[1]:


import logging, os
import pickle
logging.disable(logging.WARNING)
logging.disable(logging.INFO)
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"


# In[2]:


import tensorflow as tf
import keras.api._v2.keras as keras
from keras import layers
from keras import callbacks


# In[3]:


from sklearn.metrics import ConfusionMatrixDisplay
from sklearn.metrics import confusion_matrix


# In[4]:


import matplotlib.pyplot as plt


# In[5]:


from tensorflow.keras.applications.vgg16 import preprocess_input, decode_predictions
from tensorflow.keras.applications.mobilenet import MobileNet
MobileNetModel = MobileNet(input_shape=(416, 416, 3), include_top=False)
MobileNetModel.summary()
MobileNetModel.trainable = False


# In[6]:


BATCH_SIZE = 32
IMG_HEIGHT = 416
IMG_WIDTH = 416
#T_DATASET_PATH = r'C:\Users\samuel.martins\Anaconda3\aulas\imagem\aula 5\Desafio\cats_and_dogs\cats_and_dogs\train'
#V_DATASET_PATH = r'C:\Users\samuel.martins\Anaconda3\aulas\imagem\aula 5\Desafio\cats_and_dogs\cats_and_dogs\validation'
T_DATASET_PATH = input("Enter train data set file path here: ");

if os.path.isdir(T_DATASET_PATH):
    print("Valid train data set file path")

else:
    print("Invalid train data set file path")

SEED = 1245
TRAIN_SPLIT = 0.2



# In[7]:


train_ds = tf.keras.utils.image_dataset_from_directory(
  T_DATASET_PATH,
  labels='inferred',
  label_mode = 'categorical',
  validation_split=TRAIN_SPLIT,
  subset="training",
  seed=SEED,
  image_size=(IMG_HEIGHT, IMG_WIDTH),
  batch_size=BATCH_SIZE)


# In[8]:


val_ds = tf.keras.utils.image_dataset_from_directory(
  T_DATASET_PATH,
  labels='inferred',
  label_mode = 'categorical',
  validation_split=TRAIN_SPLIT,
  subset="validation",
  seed=SEED,
  image_size=(IMG_HEIGHT, IMG_WIDTH),
  batch_size=BATCH_SIZE)


# In[9]:


labels = train_ds.class_names
f = open(r"C:\Users\Tiago\Desktop\projeto_apvc\config\mobile_netModel\labels.pickle", "wb")
f.write(pickle.dumps(labels))
f.close()
print(labels)


# In[10]:


train_ds = train_ds.cache()
val_ds = val_ds.cache()


# optimazacoes para manter a imagens em memoria<br>
# rain_ds = train_ds.cache()<br>
# al_ds = val_ds.cache()

# nota - os layers de data augmentation originam warnings (em versoes do tensorflow superiores a 2.8.3)<br>
# esses warnings sao para ignorar

# In[11]:


model = tf.keras.models.Sequential([
    
    layers.Rescaling(1./255, input_shape=(IMG_HEIGHT, IMG_WIDTH, 3)),
    layers.RandomFlip("horizontal"),
    layers.RandomRotation(0.1),
    
    MobileNetModel,

    layers.Flatten(),
    layers.Dropout(0.1),
    layers.Dense(256, activation='relu'),
    layers.Dropout(0.1),
    layers.Flatten(),
    layers.Dropout(0.1),
    layers.Dense(512, activation='relu'),
    #layers.BatchNormalization(),
    layers.Dense(len(labels), activation="softmax")
])


# In[12]:


model.compile(optimizer='adam', loss=tf.keras.losses.CategoricalCrossentropy(), metrics=['accuracy'])


# In[13]:


model.summary()


# In[14]:


earlystopping = callbacks.EarlyStopping(monitor ="val_loss", 
                                        mode ="min", patience = 5, 
                                        restore_best_weights = True,
                                       verbose=1)


# In[ ]:


EPOCHS = 100
history = model.fit(train_ds, epochs=EPOCHS, validation_data=val_ds,callbacks =[earlystopping])

model.save(r"C:\Users\Tiago\Desktop\projeto_apvc\config\mobile_netModel")


# opter as predicoes e ground thruth num formato mais facil de tratar para mostrar os resultados<br>
# (um vetor de ids das classes)

# In[ ]:


y_pred = model.predict(val_ds)
y_pred = tf.argmax(y_pred, axis=1)


# In[ ]:


y_true = tf.concat([y for x, y in val_ds], axis=0)
y_true = tf.argmax(y_true, axis=1)


# gerar graficos e matriz de confusao

# In[ ]:


cm = confusion_matrix(y_true, y_pred)


# In[ ]:


acc = history.history['accuracy']
val_acc = history.history['val_accuracy']
loss = history.history['loss']
val_loss = history.history['val_loss']
epochs_range = range((len(history.history['accuracy'])))


# evolucao da loss e acertos

# In[ ]:


plt.figure(2, figsize=(10, 6))
plt.subplot(1, 2, 1)
plt.plot(epochs_range, acc, label='Training Accuracy')
plt.plot(epochs_range, val_acc, label='Validation Accuracy')
plt.legend(loc='lower right')
plt.title('Training and Validation Accuracy')
plt.subplot(1, 2, 2)
plt.plot(epochs_range, loss, label='Training Loss')
plt.plot(epochs_range, val_loss, label='Validation Loss')
plt.legend(loc='upper right')
plt.title('Training and Validation Loss')


# matriz de confusao

# In[ ]:


disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=labels)
disp.plot(cmap=plt.cm.Blues)
plt.show()

