
import {Fern, ValueHandler} from "../../main/javascript/fern.mjs";

{ // simple
    const fern = new Fern('hello "there" general "kenobi"');
    const object = fern.toObject();
    console.assert(object['hello'] === 'there', JSON.stringify(object));
    console.assert(object['general'] === 'kenobi', JSON.stringify(object));
}


{ // map
    const fern = new Fern('map (hello "there" general "kenobi") thing "hello"');
    const object = fern.toObject();
    console.assert(object.map['hello'] === 'there', JSON.stringify(object));
    console.assert(object.map['general'] === 'kenobi', JSON.stringify(object));
    console.assert(object['thing'] === 'hello', JSON.stringify(object));
}


{ // list
    const fern = new Fern('list ["hello" "there"]');
    const object = fern.toObject();
    console.assert(object['list'].includes('hello'), JSON.stringify(object));
    console.assert(object['list'].includes('there'), JSON.stringify(object));
}

console.log("All tests finished.");
