
class ValueHandler {

    handle(data) {
        const char = data.char;
        return false;
    }

    result(data) {
        return null;
    }

}

class StringHandler extends ValueHandler {

    handle(data) {
        if (!data['started']) {
            data['escape'] = false;
            return data['started'] = true;
        }
        const char = data.char;
        if (char === '"' && !data['escape']) return false;
        if (!data['escape'] && char === '\\') data['escape'] = true;
        else {
            data.value += char;
            data['escape'] = false;
        }
        return true;
    }

    result(data) {
        return data.value;
    }
}

class Fern {
    handlers = {};
    data;
    constructor(data) {
        this.data = data;
        this.handlers['"'] = new StringHandler();
    }

    toObject() {
        if (Array.isArray(this.data)) return this.data;
        if (typeof this.data === 'string' || this.data instanceof String) return this.stringToObject(this.data);
        return this.data;
    }

    toString() {
        if (typeof this.data === 'string' || this.data instanceof String) return this.data;
        if (Array.isArray(this.data)) return Fern.listToString(this.data);
        return Fern.objectToString(this.data);
    }

    _text = '';
    _object = {};
    _current = this._object;

    readList() {
        let state = 0, handler = new ValueHandler();
        let value = {char: '', value: '', result: null, fern: this};
        check: while (this._text.length > 0) {
            const char = this._text.charAt(0);
            this._text = this._text.substring(1);
            switch (state) {
                case 0: // whitespace pre-value
                    if (/\s/.test(char)) continue;
                    else if (char === ']') break check;
                    if (char === '(' || char === '[') state = 5; // sub branch
                    else { // literal value
                        value.value = '';
                        value.char = char;
                        state = 1; // prepare for reading value
                        escape = false;
                        handler = this.handlers[char];
                        if (handler == null) throw new Error('No handler found for character ' + char);
                        handler.handle(value);
                        break;
                    }
                case 2: // detected a branch value
                    const previous = this._current;
                    if (char === '(') {
                        const next = {};
                        previous.push(next);
                        this._current = next;
                        this.readMap();
                    } else {
                        const next = [];
                        previous.push(next);
                        this._current = next;
                        this.readList();
                    }
                    this._current = previous;
                    state = 0;
                    break;
                case 1: // in value
                    value.char = char;
                    if (handler.handle(value)) break;
                    this._current.push(handler.result(value));
                    if (char === ']') break check;
                    else state = 0;
                    break;
            }
        }
        return this._current;
    }

    readMap() {
        let state = 0, escape = false, handler = new ValueHandler();
        let key = '', value = {char: '', value: '', result: null, fern: this};
        check: while (this._text.length > 0) {
            const char = this._text.charAt(0);
            this._text = this._text.substring(1);
            switch (state) {
                case 0: // whitespace pre-key
                    if (/\s/.test(char)) continue;
                    else if (char === ')') break check;
                    state = 1;
                    escape = false;
                    key = char;
                    break;
                case 1: // reading key
                    if (!escape && /\s/.test(char)) {
                        state = 2; // end of key
                        break;
                    } else if (!escape && char === '\\') escape = true;
                    else {
                        key += char;
                        escape = false;
                    }
                    break;
                case 2: // after key
                    if (/\s/.test(char)) continue;
                    if (char === '(' || char === '[') state = 5; // sub branch
                    else { // literal value
                        value.value = '';
                        value.char = char;
                        state = 3; // prepare for reading value
                        escape = false;
                        handler = this.handlers[char];
                        if (handler == null) throw new Error('No handler found for character ' + char);
                        handler.handle(value);
                        break;
                    }
                case 5: // detected a branch value
                    const previous = this._current;
                    if (char === '(') {
                        const next = {};
                        previous[key] = next;
                        this._current = next;
                        this.readMap();
                    } else {
                        const next = [];
                        previous[key] = next;
                        this._current = next;
                        this.readList();
                    }
                    this._current = previous;
                    state = 0;
                    break;
                case 3: // in value
                    value.char = char;
                    if (handler.handle(value)) break;
                    this._current[key] = handler.result(value);
                    if (char === ')') break check;
                    else state = 0;
                    break;
            }
        }
        return this._current;
    }

    stringToObject(string) {
        this._text = string;
        return this.readMap();
    }

    static listToString(list) {
        let string = '[ ';
        for (const value of list) {
            string += Fern.valueToString(value);
            string += ' ';
        }
        string += ']';
        return string;
    }

    static objectToString(object) {
        let string = '( ';
        for (let key in object) {
            string += key + ' ';
            string += Fern.valueToString(object[key]);
            string += ' ';
        }
        string += ')';
        return string;
    }

    static valueToString(value) {
        if (typeof value === 'string' || value instanceof String) return '"' + value + '"';
        return value + '';
    }

}

export {Fern, ValueHandler}
