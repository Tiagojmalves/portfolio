import cv2
import numpy as np
import os
import glob
import pickle
from keras.preprocessing import image
import tensorflow as tf
import xml.etree.ElementTree as ET


WIDTH = 416  # largura da imagem no YOLO
HEIGHT = 416 # altura da imagem no YOLO

CONFIDENCE_THRESHOLD = 0.5 # Threshold para a confianca nas bounding box
NMS_THRESHOLD = 0.5       # Threshold para o algoritmo non maximum supression

u_input = input("\n Enter file path here: ");

if os.path.isdir(u_input):

    names = [os.path.basename(x) for x in glob.glob(u_input + "/*jpg")]

else:
    print("\n Directory does not exist.")

dirName = "/Crop"

try:
    # Create target Directory
    os.mkdir(u_input+dirName)
    print("Directory " , dirName ,  " Created ")

except FileExistsError:
    print("Directory " , dirName ,  " already exists")

MODEL_FILE = r"C:\Users\Tiago\Desktop\projeto_apvc\config\Yolo_plus_mobilenet\yolov3_ts_train_final.weights"
CONFIG_FILE = r"C:\Users\Tiago\Desktop\projeto_apvc\config\Yolo_plus_mobilenet\yolov3_ts_train.cfg"
CLASS_FILE = r"C:\Users\Tiago\Desktop\projeto_apvc\config\classes_1.names"


with open(CLASS_FILE, 'r') as f:
    class_names = f.read().split('\n')

# gerar cores aleatoriamente para cada uma das classes
COLORS = np.random.uniform(0, 255, size=(len(class_names), 3))


YoloModel = cv2.dnn.readNetFromDarknet(cfgFile=CONFIG_FILE, darknetModel=MODEL_FILE)
YoloModel.setPreferableBackend(cv2.dnn.DNN_BACKEND_OPENCV)

ln = YoloModel.getLayerNames()
ln = [ln[i - 1] for i in YoloModel.getUnconnectedOutLayers()]

for file in names:


    img = u_input + "/" + file
    img = cv2.imread(img)
    img_height, img_width, img_channels = img.shape
    blob = cv2.dnn.blobFromImage(image=img, scalefactor=1 / 255.0, size=(WIDTH, HEIGHT), swapRB=True)
    YoloModel.setInput(blob)
    outputs = YoloModel.forward(ln)

    bboxes = []
    confidences = []
    classIDs = []

    for output in outputs:
        for detection in output:
            # obter o valor da confianca da classe com maior confianca
            scores = detection[5:]
            classID = np.argmax(scores)
            confidence = scores[classID]

            if confidence > CONFIDENCE_THRESHOLD:

                # obter as coordenadas e dimensoes das bounding boxes, normalizadas para coordenadas da imagem
                bbox_center_x = detection[0] * img_width
                bbox_center_y = detection[1] * img_height
                bbox_width = detection[2] * img_width
                bbox_height = detection[3] * img_height

                bbox_x = int(bbox_center_x - (bbox_width / 2))
                bbox_y = int(bbox_center_y - (bbox_height / 2))

                bboxes.append([bbox_x, bbox_y, int(bbox_width), int(bbox_height)])
                confidences.append(float(confidence))
                classIDs.append(classID)

        idxs = cv2.dnn.NMSBoxes(bboxes, confidences, CONFIDENCE_THRESHOLD, NMS_THRESHOLD)

        if len(idxs) > 0:
            for i in idxs.flatten():
                # extrair as coordenadas e dimensoes das bounding boxes resultantes
                (bbox_x, bbox_y) = (bboxes[i][0], bboxes[i][1])
                (bbox_w, bbox_h) = (bboxes[i][2], bboxes[i][3])

                # colocar retangulos e texto a marcar os objetos identificados
                class_name = class_names[classIDs[i]]
                color = COLORS[classIDs[i]]
                #cv2.rectangle(img, (bbox_x, bbox_y), (bbox_x + bbox_w, bbox_y + bbox_h), color, 2)
                text = "{}: {:.4f}".format(class_name, confidences[i])
                #cv2.putText(img, text, (bbox_x, bbox_y - 5), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 1)

                cropped_image = img[bbox_y:bbox_y+bbox_h, bbox_x:bbox_x+bbox_w]
                cv2.imwrite(f"{u_input}/{dirName}/crop_{i}_{file}", cropped_image)

    #cv2.imshow('image', img)
    #cv2.waitKey()
    #cv2.destroyAllWindows()


model = tf.keras.models.load_model(r"C:\Users\Tiago\Desktop\projeto_apvc\config\mobile_netModel")
#model = tf.keras.models.load_model(r"C:\Users\Tiago\Desktop\projeto_apvc\config\mobile_netModel_undersampling")

path = f"{u_input}/{dirName}"


if os.path.isdir(path):

    names_1 = [os.path.basename(x) for x in glob.glob(path + "/*jpg")]

else:
    print("\n Directory does not exist.")

class_names = pickle.loads(open(r"C:\Users\Tiago\Desktop\projeto_apvc\config\mobile_netModel\labels.pickle", "rb").read())


for file in names_1:

    img = path + "/" + file
    img_og = cv2.imread(img)
    img_og = cv2.resize(img_og, (WIDTH,HEIGHT), interpolation=cv2.INTER_AREA)

    img_height, img_width, img_channels = img_og.shape
    img = np.expand_dims(img_og, axis=0)
    classes = model.predict(img)
    classes = class_names[classes.argmax(axis=1)[0]]
    cv2.putText(img_og, classes, (20, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 1, cv2.LINE_AA)
    print(classes)

    cv2.imshow("prediction", img_og)
    cv2.waitKey()
    cv2.destroyAllWindows()
