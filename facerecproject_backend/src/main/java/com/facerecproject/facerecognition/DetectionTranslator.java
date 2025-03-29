package com.facerecproject.facerecognition;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.List;

public class DetectionTranslator implements Translator<Image, List<Detection>> {
    @Override
    public NDList processInput(TranslatorContext translatorContext, Image image) throws Exception {
// A bemeneti képet előkészítjük: átméretezés, normalizálás, stb.
        NDArray array = image.toNDArray(translatorContext.getNDManager()); // A kép NDArray-ként való előkészítése
        return new NDList(array);    }

    @Override
    public List<Detection> processOutput(TranslatorContext translatorContext, NDList ndList) throws Exception {
        NDArray array = ndList.getFirst(); // Az első NDArray elem
        return Detection.fromNDArray(array); // Az outputot Detection objektumokká alakítjuk
    }

    @Override
    public Batchifier getBatchifier() {
        return Translator.super.getBatchifier();
    }
}
