<?php

class FernParser {

    public function parse($text) {
        $text = str_replace("\n", " ", $text);
        $lines = explode(" ", $text);
        $result = [];
        $nextIndex = -1;
        $skippedIndexes = [];
        foreach ($lines as $index => $key) {
            if ($index == $nextIndex) continue;
            if (isset($skippedIndexes[$index])) continue;
            if ($this->startsWith('"', $key)) {
                $result[] = $this->stripText($key);
                $nextIndex = $nextIndex - 1;
                continue;
            }
            $nextIndex = $index + 1;
            if (!isset($lines[$nextIndex])) return $result;
            $value = trim($lines[$nextIndex]);
            $value = $this->stripText($value);
            $key = trim($key);
            if ($key == "(") {
                $value = "(";
                $nextIndex--;
            }
            if ($this->endsWith("F", $value) || $this->endsWith("D", $value)) {
                $value = substr($value, 0, -1);
                $value = (float)$value;
                $result[$key] = $value;
            } else if ($this->endsWith("B", $value) || $this->endsWith("S", $value) || $this->endsWith("L", $value)) {
                $value = substr($value, 0, -1);
                $value = (integer)$value;
                $result[$key] = $value;
            } else if ($value == "(") { // map - reassemble the map keys and values
                $mapOpenTags = 1;
                $mapIndex = $nextIndex;
                $mapText = [];
                while ($mapOpenTags > 0) {
                    $mapIndex++;
                    $mapValue = $lines[$mapIndex];
                    $skippedIndexes[$mapIndex] = true;
                    if (trim($mapValue) == "") continue;
                    else if ($mapValue == "(") $mapOpenTags++;
                    else if ($mapValue == ")" && --$mapOpenTags == 0) continue;
                    $mapText[] = $mapValue;
                }
                if ($key == "(") $result[] = $this->parse(implode(" ", $mapText));
                else $result[$key] = $this->parse(implode(" ", $mapText));
            } else if ($value == "[") { // list
                $listOpenTags = 1;
                $listIndex = $nextIndex;
                $listText = [];
                while ($listOpenTags > 0) {
                    $listIndex++;
                    $listValue = $lines[$listIndex];
                    $skippedIndexes[$listIndex] = true;
                    if (trim($listValue) == "") continue;
                    else if ($listValue == "[") $listOpenTags++;
                    else if ($listValue == "]" && --$listOpenTags == 0) continue;
                    $listText[] = $listValue;
                }
                $result[$key] = $this->parse(implode(" ", $listText));
            } else if ($value == "false" || $value == "true") $result[$key] = (boolean)$value;
            else if ($value == "null") $result[$key] = null;
            else if (is_int($value)) $result[$key] = (integer)$value;
            else if (is_numeric($value)) $result[$key] = (float)$value;
            else $result[$key] = $value;
        }
        return $result;
    }

    public function startsWith($needle, $haystack) {
        $length = strlen($needle);
        return !is_array($haystack) && substr($haystack, 0, $length) === $needle;
    }

    public function stripText($text) {
        return str_replace('"', '', $text);
    }

    public function endsWith($needle, $haystack) {
        $length = strlen($needle);
        return $length == 0 || substr($haystack, -$length) === $needle;
    }

}
