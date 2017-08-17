'use strict';

var util = require('util');
var EventEmitter = require('events').EventEmitter;

var {
    NativeEventEmitter,
    NativeModules
} = require('react-native');

var Native = NativeModules.ServiceDiscovery;

function ServiceDiscovery() {
    this._eventEmitter = new NativeEventEmitter(Native);

    if (EventEmitter instanceof Function) {
        EventEmitter.call(this);
    }

    if (!this.on) {
        this.on = this.addListener.bind(this);
    }
}

util.inherits(ServiceDiscovery, EventEmitter);

ServiceDiscovery.prototype.start = function() {
    this._registerEvents();
    Native.start();
};

ServiceDiscovery.prototype.stop = function() {
    Native.stop();
};

ServiceDiscovery.prototype._registerEvents = function() {
    if (this._subs && this._subs.length > 0) {
        return;
    }

    this._subs = [
        this._eventEmitter.addListener('service-resolved', ev => {
            if (this._id !== ev.id) {
                return;
            }
            this._onServiceResolved(ev);
        }),
        this._eventEmitter.addListener('udp-discovery', ev => {
            if (this._id !== ev.id) {
                return;
            }
            this._onUdpDiscovery(ev);
        }),
    ];
};

ServiceDiscovery.prototype._onServiceResolved = function(ev) {
    this.emit('service-resolved', ev);
};

ServiceDiscovery.prototype._onUdpDiscovery = function(ev) {
    this.emit('udp-discovery', ev);
};

ServiceDiscovery.prototype._unregisterEvents = function() {
    this._subs.forEach(e => e.remove());
    this._subs = [];
};

module.exports = ServiceDiscovery;
