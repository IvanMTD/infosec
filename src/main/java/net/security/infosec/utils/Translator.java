package net.security.infosec.utils;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Translator {
    public static String transliterate(String text) {
        Map<Character, String> singleReplacements = new HashMap<>();
        singleReplacements.put('А', "A");
        singleReplacements.put('а', "a");
        singleReplacements.put('Б', "B");
        singleReplacements.put('б', "b");
        singleReplacements.put('В', "V");
        singleReplacements.put('в', "v");
        singleReplacements.put('Г', "G");
        singleReplacements.put('г', "g");
        singleReplacements.put('Д', "D");
        singleReplacements.put('д', "d");
        singleReplacements.put('Е', "E");
        singleReplacements.put('е', "e");
        singleReplacements.put('Ё', "Yo");
        singleReplacements.put('ё', "yo");
        singleReplacements.put('Ж', "Zh");
        singleReplacements.put('ж', "zh");
        singleReplacements.put('З', "Z");
        singleReplacements.put('з', "z");
        singleReplacements.put('И', "I");
        singleReplacements.put('и', "i");
        singleReplacements.put('Й', "Y");
        singleReplacements.put('й', "y");
        singleReplacements.put('К', "K");
        singleReplacements.put('к', "k");
        singleReplacements.put('Л', "L");
        singleReplacements.put('л', "l");
        singleReplacements.put('М', "M");
        singleReplacements.put('м', "m");
        singleReplacements.put('Н', "N");
        singleReplacements.put('н', "n");
        singleReplacements.put('О', "O");
        singleReplacements.put('о', "o");
        singleReplacements.put('П', "P");
        singleReplacements.put('п', "p");
        singleReplacements.put('Р', "R");
        singleReplacements.put('р', "r");
        singleReplacements.put('С', "S");
        singleReplacements.put('с', "s");
        singleReplacements.put('Т', "T");
        singleReplacements.put('т', "t");
        singleReplacements.put('У', "U");
        singleReplacements.put('у', "u");
        singleReplacements.put('Ф', "F");
        singleReplacements.put('ф', "f");
        singleReplacements.put('Х', "H");
        singleReplacements.put('х', "h");
        singleReplacements.put('Ц', "Ts");
        singleReplacements.put('ц', "ts");
        singleReplacements.put('Ч', "Ch");
        singleReplacements.put('ч', "ch");
        singleReplacements.put('Ш', "Sh");
        singleReplacements.put('ш', "sh");
        singleReplacements.put('Щ', "Shch");
        singleReplacements.put('щ', "shch");
        singleReplacements.put('Ъ', "");
        singleReplacements.put('ъ', "");
        singleReplacements.put('Ы', "Y");
        singleReplacements.put('ы', "y");
        singleReplacements.put('Ь', "");
        singleReplacements.put('ь', "");
        singleReplacements.put('Э', "E");
        singleReplacements.put('э', "e");
        singleReplacements.put('Ю', "Yu");
        singleReplacements.put('ю', "yu");
        singleReplacements.put('Я', "Ya");
        singleReplacements.put('я', "ya");

        Map<String, String> doubleReplacements = new HashMap<>();
        doubleReplacements.put("ья", "ya");
        doubleReplacements.put("ью", "yu");
        doubleReplacements.put("ье", "ye");
        doubleReplacements.put("ьо", "yo");
        doubleReplacements.put("ё", "yo");
        doubleReplacements.put("жь", "zh");
        doubleReplacements.put("чь", "ch");
        doubleReplacements.put("шь", "sh");
        doubleReplacements.put("щь", "shch");
        doubleReplacements.put("ць", "ts");
        doubleReplacements.put("дь", "d");
        doubleReplacements.put("ть", "t");
        doubleReplacements.put("нь", "n");
        doubleReplacements.put("ль", "l");
        doubleReplacements.put("мя", "m");
        doubleReplacements.put("бь", "b");
        doubleReplacements.put("пь", "p");
        doubleReplacements.put("вь", "v");
        doubleReplacements.put("гь", "g");
        doubleReplacements.put("йь", "y");
        doubleReplacements.put("кь", "k");
        doubleReplacements.put("мь", "m");
        doubleReplacements.put("фь", "f");
        doubleReplacements.put("хь", "h");
        doubleReplacements.put("ъ", "");
        doubleReplacements.put("ы", "y");
        doubleReplacements.put("ь", "");
        doubleReplacements.put("э", "e");
        doubleReplacements.put("ю", "yu");
        doubleReplacements.put("я", "ya");

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String replacement = singleReplacements.getOrDefault(c, String.valueOf(c));
            result.append(replacement);

            if (i < text.length() - 1) {
                String c1 = String.valueOf(c);
                String c2 = String.valueOf(text.charAt(i + 1));
                String doubleChar = c1 + c2;
                String doubleReplacement = doubleReplacements.get(doubleChar);
                if (doubleReplacement != null) {
                    result.append(doubleReplacement);
                    i++;
                }
            }
        }

        String normalizedResult = Normalizer.normalize(result.toString(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();

        return Pattern.compile("[-]+").matcher(normalizedResult).replaceAll("-");
    }
}
