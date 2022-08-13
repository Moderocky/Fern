import {Fern} from "../../main/javascript/fern.mjs";

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


{ // booleans
    const fern = new Fern('test true thing false blob true');
    const object = fern.toObject();
    console.assert(object['test'] === true, JSON.stringify(object));
    console.assert(object['thing'] === false, JSON.stringify(object));
    console.assert(object['blob'] === true, JSON.stringify(object));
}


{ // numbers
    const fern = new Fern('test 123 thing -12.5F test 3');
    const object = fern.toObject();
    console.assert(object['test'] === 3, JSON.stringify(object));
    console.assert(object['thing'] === -12.5, JSON.stringify(object));
}


{ // null
    const fern = new Fern('test 123 thing null');
    const object = fern.toObject();
    console.assert(object['test'] === 123, JSON.stringify(object));
    console.assert(object['thing'] === null, JSON.stringify(object));
}


console.log("All tests finished.");
