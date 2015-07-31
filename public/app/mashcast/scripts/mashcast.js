/**
 * Mashcast.FM core code
 */

var observable = (function (exports) {
/**
 *@from {@link http://stackoverflow.com/questions/9671995/javascript-custom-event-listener} 
 */
    var Observable = function() {
    };
    Observable.prototype = {
        addEventListener: function(type, method) {
            var listeners, handlers, scope;
            if (!(listeners = this.listeners)) {
                listeners = this.listeners = {};
            }
            if (!(handlers = listeners[type])){
                handlers = listeners[type] = [];
            }
            scope = (scope ? scope : window);
            handlers.push({
                method: method,
                scope: scope,
               // context: (context ? context : scope)
            });
        },
        dispatchEvent: function(type, data) {
            var listeners, handlers, i, n, handler, scope;
            if (!(listeners = this.listeners)) {
                return;
            }
            if (!(handlers = listeners[type])){
                return;
            }
            for (i = 0, n = handlers.length; i < n; i++){
                handler = handlers[i];
                //if (typeof(context)!=="undefined" && context !== handler.context) continue;
                if (handler.method.call(
                    handler.scope, this, type, data
                )===false) {
                    return false;
                }
            }
            return true;
        }
    };
    exports.Observable = Observable;
});
observable(window);

/*!
 * @overview es6-promise - a tiny implementation of Promises/A+.
 * @copyright Copyright (c) 2014 Yehuda Katz, Tom Dale, Stefan Penner and contributors (Conversion to ES6 API by Jake Archibald)
 * @license   Licensed under MIT license
 *            See https://raw.githubusercontent.com/jakearchibald/es6-promise/master/LICENSE
 * @version   2.0.0
 */

(function() {
    "use strict";

    function $$utils$$objectOrFunction(x) {
      return typeof x === 'function' || (typeof x === 'object' && x !== null);
    }

    function $$utils$$isFunction(x) {
      return typeof x === 'function';
    }

    function $$utils$$isMaybeThenable(x) {
      return typeof x === 'object' && x !== null;
    }

    var $$utils$$_isArray;

    if (!Array.isArray) {
      $$utils$$_isArray = function (x) {
        return Object.prototype.toString.call(x) === '[object Array]';
      };
    } else {
      $$utils$$_isArray = Array.isArray;
    }

    var $$utils$$isArray = $$utils$$_isArray;
    var $$utils$$now = Date.now || function() { return new Date().getTime(); };
    function $$utils$$F() { }

    var $$utils$$o_create = (Object.create || function (o) {
      if (arguments.length > 1) {
        throw new Error('Second argument not supported');
      }
      if (typeof o !== 'object') {
        throw new TypeError('Argument must be an object');
      }
      $$utils$$F.prototype = o;
      return new $$utils$$F();
    });

    var $$asap$$len = 0;

    var $$asap$$default = function asap(callback, arg) {
      $$asap$$queue[$$asap$$len] = callback;
      $$asap$$queue[$$asap$$len + 1] = arg;
      $$asap$$len += 2;
      if ($$asap$$len === 2) {
        // If len is 1, that means that we need to schedule an async flush.
        // If additional callbacks are queued before the queue is flushed, they
        // will be processed by this flush that we are scheduling.
        $$asap$$scheduleFlush();
      }
    };

    var $$asap$$browserGlobal = (typeof window !== 'undefined') ? window : {};
    var $$asap$$BrowserMutationObserver = $$asap$$browserGlobal.MutationObserver || $$asap$$browserGlobal.WebKitMutationObserver;

    // test for web worker but not in IE10
    var $$asap$$isWorker = typeof Uint8ClampedArray !== 'undefined' &&
      typeof importScripts !== 'undefined' &&
      typeof MessageChannel !== 'undefined';

    // node
    function $$asap$$useNextTick() {
      return function() {
        process.nextTick($$asap$$flush);
      };
    }

    function $$asap$$useMutationObserver() {
      var iterations = 0;
      var observer = new $$asap$$BrowserMutationObserver($$asap$$flush);
      var node = document.createTextNode('');
      observer.observe(node, { characterData: true });

      return function() {
        node.data = (iterations = ++iterations % 2);
      };
    }

    // web worker
    function $$asap$$useMessageChannel() {
      var channel = new MessageChannel();
      channel.port1.onmessage = $$asap$$flush;
      return function () {
        channel.port2.postMessage(0);
      };
    }

    function $$asap$$useSetTimeout() {
      return function() {
        setTimeout($$asap$$flush, 1);
      };
    }

    var $$asap$$queue = new Array(1000);

    function $$asap$$flush() {
      for (var i = 0; i < $$asap$$len; i+=2) {
        var callback = $$asap$$queue[i];
        var arg = $$asap$$queue[i+1];

        callback(arg);

        $$asap$$queue[i] = undefined;
        $$asap$$queue[i+1] = undefined;
      }

      $$asap$$len = 0;
    }

    var $$asap$$scheduleFlush;

    // Decide what async method to use to triggering processing of queued callbacks:
    if (typeof process !== 'undefined' && {}.toString.call(process) === '[object process]') {
      $$asap$$scheduleFlush = $$asap$$useNextTick();
    } else if ($$asap$$BrowserMutationObserver) {
      $$asap$$scheduleFlush = $$asap$$useMutationObserver();
    } else if ($$asap$$isWorker) {
      $$asap$$scheduleFlush = $$asap$$useMessageChannel();
    } else {
      $$asap$$scheduleFlush = $$asap$$useSetTimeout();
    }

    function $$$internal$$noop() {}
    var $$$internal$$PENDING   = void 0;
    var $$$internal$$FULFILLED = 1;
    var $$$internal$$REJECTED  = 2;
    var $$$internal$$GET_THEN_ERROR = new $$$internal$$ErrorObject();

    function $$$internal$$selfFullfillment() {
      return new TypeError("You cannot resolve a promise with itself");
    }

    function $$$internal$$cannotReturnOwn() {
      return new TypeError('A promises callback cannot return that same promise.')
    }

    function $$$internal$$getThen(promise) {
      try {
        return promise.then;
      } catch(error) {
        $$$internal$$GET_THEN_ERROR.error = error;
        return $$$internal$$GET_THEN_ERROR;
      }
    }

    function $$$internal$$tryThen(then, value, fulfillmentHandler, rejectionHandler) {
      try {
        then.call(value, fulfillmentHandler, rejectionHandler);
      } catch(e) {
        return e;
      }
    }

    function $$$internal$$handleForeignThenable(promise, thenable, then) {
       $$asap$$default(function(promise) {
        var sealed = false;
        var error = $$$internal$$tryThen(then, thenable, function(value) {
          if (sealed) { return; }
          sealed = true;
          if (thenable !== value) {
            $$$internal$$resolve(promise, value);
          } else {
            $$$internal$$fulfill(promise, value);
          }
        }, function(reason) {
          if (sealed) { return; }
          sealed = true;

          $$$internal$$reject(promise, reason);
        }, 'Settle: ' + (promise._label || ' unknown promise'));

        if (!sealed && error) {
          sealed = true;
          $$$internal$$reject(promise, error);
        }
      }, promise);
    }

    function $$$internal$$handleOwnThenable(promise, thenable) {
      if (thenable._state === $$$internal$$FULFILLED) {
        $$$internal$$fulfill(promise, thenable._result);
      } else if (promise._state === $$$internal$$REJECTED) {
        $$$internal$$reject(promise, thenable._result);
      } else {
        $$$internal$$subscribe(thenable, undefined, function(value) {
          $$$internal$$resolve(promise, value);
        }, function(reason) {
          $$$internal$$reject(promise, reason);
        });
      }
    }

    function $$$internal$$handleMaybeThenable(promise, maybeThenable) {
      if (maybeThenable.constructor === promise.constructor) {
        $$$internal$$handleOwnThenable(promise, maybeThenable);
      } else {
        var then = $$$internal$$getThen(maybeThenable);

        if (then === $$$internal$$GET_THEN_ERROR) {
          $$$internal$$reject(promise, $$$internal$$GET_THEN_ERROR.error);
        } else if (then === undefined) {
          $$$internal$$fulfill(promise, maybeThenable);
        } else if ($$utils$$isFunction(then)) {
          $$$internal$$handleForeignThenable(promise, maybeThenable, then);
        } else {
          $$$internal$$fulfill(promise, maybeThenable);
        }
      }
    }

    function $$$internal$$resolve(promise, value) {
      if (promise === value) {
        $$$internal$$reject(promise, $$$internal$$selfFullfillment());
      } else if ($$utils$$objectOrFunction(value)) {
        $$$internal$$handleMaybeThenable(promise, value);
      } else {
        $$$internal$$fulfill(promise, value);
      }
    }

    function $$$internal$$publishRejection(promise) {
      if (promise._onerror) {
        promise._onerror(promise._result);
      }

      $$$internal$$publish(promise);
    }

    function $$$internal$$fulfill(promise, value) {
      if (promise._state !== $$$internal$$PENDING) { return; }

      promise._result = value;
      promise._state = $$$internal$$FULFILLED;

      if (promise._subscribers.length === 0) {
      } else {
        $$asap$$default($$$internal$$publish, promise);
      }
    }

    function $$$internal$$reject(promise, reason) {
      if (promise._state !== $$$internal$$PENDING) { return; }
      promise._state = $$$internal$$REJECTED;
      promise._result = reason;

      $$asap$$default($$$internal$$publishRejection, promise);
    }

    function $$$internal$$subscribe(parent, child, onFulfillment, onRejection) {
      var subscribers = parent._subscribers;
      var length = subscribers.length;

      parent._onerror = null;

      subscribers[length] = child;
      subscribers[length + $$$internal$$FULFILLED] = onFulfillment;
      subscribers[length + $$$internal$$REJECTED]  = onRejection;

      if (length === 0 && parent._state) {
        $$asap$$default($$$internal$$publish, parent);
      }
    }

    function $$$internal$$publish(promise) {
      var subscribers = promise._subscribers;
      var settled = promise._state;

      if (subscribers.length === 0) { return; }

      var child, callback, detail = promise._result;

      for (var i = 0; i < subscribers.length; i += 3) {
        child = subscribers[i];
        callback = subscribers[i + settled];

        if (child) {
          $$$internal$$invokeCallback(settled, child, callback, detail);
        } else {
          callback(detail);
        }
      }

      promise._subscribers.length = 0;
    }

    function $$$internal$$ErrorObject() {
      this.error = null;
    }

    var $$$internal$$TRY_CATCH_ERROR = new $$$internal$$ErrorObject();

    function $$$internal$$tryCatch(callback, detail) {
      try {
        return callback(detail);
      } catch(e) {
        $$$internal$$TRY_CATCH_ERROR.error = e;
        return $$$internal$$TRY_CATCH_ERROR;
      }
    }

    function $$$internal$$invokeCallback(settled, promise, callback, detail) {
      var hasCallback = $$utils$$isFunction(callback),
          value, error, succeeded, failed;

      if (hasCallback) {
        value = $$$internal$$tryCatch(callback, detail);

        if (value === $$$internal$$TRY_CATCH_ERROR) {
          failed = true;
          error = value.error;
          value = null;
        } else {
          succeeded = true;
        }

        if (promise === value) {
          $$$internal$$reject(promise, $$$internal$$cannotReturnOwn());
          return;
        }

      } else {
        value = detail;
        succeeded = true;
      }

      if (promise._state !== $$$internal$$PENDING) {
        // noop
      } else if (hasCallback && succeeded) {
        $$$internal$$resolve(promise, value);
      } else if (failed) {
        $$$internal$$reject(promise, error);
      } else if (settled === $$$internal$$FULFILLED) {
        $$$internal$$fulfill(promise, value);
      } else if (settled === $$$internal$$REJECTED) {
        $$$internal$$reject(promise, value);
      }
    }

    function $$$internal$$initializePromise(promise, resolver) {
      try {
        resolver(function resolvePromise(value){
          $$$internal$$resolve(promise, value);
        }, function rejectPromise(reason) {
          $$$internal$$reject(promise, reason);
        });
      } catch(e) {
        $$$internal$$reject(promise, e);
      }
    }

    function $$$enumerator$$makeSettledResult(state, position, value) {
      if (state === $$$internal$$FULFILLED) {
        return {
          state: 'fulfilled',
          value: value
        };
      } else {
        return {
          state: 'rejected',
          reason: value
        };
      }
    }

    function $$$enumerator$$Enumerator(Constructor, input, abortOnReject, label) {
      this._instanceConstructor = Constructor;
      this.promise = new Constructor($$$internal$$noop, label);
      this._abortOnReject = abortOnReject;

      if (this._validateInput(input)) {
        this._input     = input;
        this.length     = input.length;
        this._remaining = input.length;

        this._init();

        if (this.length === 0) {
          $$$internal$$fulfill(this.promise, this._result);
        } else {
          this.length = this.length || 0;
          this._enumerate();
          if (this._remaining === 0) {
            $$$internal$$fulfill(this.promise, this._result);
          }
        }
      } else {
        $$$internal$$reject(this.promise, this._validationError());
      }
    }

    $$$enumerator$$Enumerator.prototype._validateInput = function(input) {
      return $$utils$$isArray(input);
    };

    $$$enumerator$$Enumerator.prototype._validationError = function() {
      return new Error('Array Methods must be provided an Array');
    };

    $$$enumerator$$Enumerator.prototype._init = function() {
      this._result = new Array(this.length);
    };

    var $$$enumerator$$default = $$$enumerator$$Enumerator;

    $$$enumerator$$Enumerator.prototype._enumerate = function() {
      var length  = this.length;
      var promise = this.promise;
      var input   = this._input;

      for (var i = 0; promise._state === $$$internal$$PENDING && i < length; i++) {
        this._eachEntry(input[i], i);
      }
    };

    $$$enumerator$$Enumerator.prototype._eachEntry = function(entry, i) {
      var c = this._instanceConstructor;
      if ($$utils$$isMaybeThenable(entry)) {
        if (entry.constructor === c && entry._state !== $$$internal$$PENDING) {
          entry._onerror = null;
          this._settledAt(entry._state, i, entry._result);
        } else {
          this._willSettleAt(c.resolve(entry), i);
        }
      } else {
        this._remaining--;
        this._result[i] = this._makeResult($$$internal$$FULFILLED, i, entry);
      }
    };

    $$$enumerator$$Enumerator.prototype._settledAt = function(state, i, value) {
      var promise = this.promise;

      if (promise._state === $$$internal$$PENDING) {
        this._remaining--;

        if (this._abortOnReject && state === $$$internal$$REJECTED) {
          $$$internal$$reject(promise, value);
        } else {
          this._result[i] = this._makeResult(state, i, value);
        }
      }

      if (this._remaining === 0) {
        $$$internal$$fulfill(promise, this._result);
      }
    };

    $$$enumerator$$Enumerator.prototype._makeResult = function(state, i, value) {
      return value;
    };

    $$$enumerator$$Enumerator.prototype._willSettleAt = function(promise, i) {
      var enumerator = this;

      $$$internal$$subscribe(promise, undefined, function(value) {
        enumerator._settledAt($$$internal$$FULFILLED, i, value);
      }, function(reason) {
        enumerator._settledAt($$$internal$$REJECTED, i, reason);
      });
    };

    var $$promise$all$$default = function all(entries, label) {
      return new $$$enumerator$$default(this, entries, true /* abort on reject */, label).promise;
    };

    var $$promise$race$$default = function race(entries, label) {
      /*jshint validthis:true */
      var Constructor = this;

      var promise = new Constructor($$$internal$$noop, label);

      if (!$$utils$$isArray(entries)) {
        $$$internal$$reject(promise, new TypeError('You must pass an array to race.'));
        return promise;
      }

      var length = entries.length;

      function onFulfillment(value) {
        $$$internal$$resolve(promise, value);
      }

      function onRejection(reason) {
        $$$internal$$reject(promise, reason);
      }

      for (var i = 0; promise._state === $$$internal$$PENDING && i < length; i++) {
        $$$internal$$subscribe(Constructor.resolve(entries[i]), undefined, onFulfillment, onRejection);
      }

      return promise;
    };

    var $$promise$resolve$$default = function resolve(object, label) {
      /*jshint validthis:true */
      var Constructor = this;

      if (object && typeof object === 'object' && object.constructor === Constructor) {
        return object;
      }

      var promise = new Constructor($$$internal$$noop, label);
      $$$internal$$resolve(promise, object);
      return promise;
    };

    var $$promise$reject$$default = function reject(reason, label) {
      /*jshint validthis:true */
      var Constructor = this;
      var promise = new Constructor($$$internal$$noop, label);
      $$$internal$$reject(promise, reason);
      return promise;
    };

    var $$es6$promise$promise$$counter = 0;

    function $$es6$promise$promise$$needsResolver() {
      throw new TypeError('You must pass a resolver function as the first argument to the promise constructor');
    }

    function $$es6$promise$promise$$needsNew() {
      throw new TypeError("Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function.");
    }

    var $$es6$promise$promise$$default = $$es6$promise$promise$$Promise;

    /**
      Promise objects represent the eventual result of an asynchronous operation. The
      primary way of interacting with a promise is through its `then` method, which
      registers callbacks to receive either a promiseÃ¢â‚¬â„¢s eventual value or the reason
      why the promise cannot be fulfilled.

      Terminology
      -----------

      - `promise` is an object or function with a `then` method whose behavior conforms to this specification.
      - `thenable` is an object or function that defines a `then` method.
      - `value` is any legal JavaScript value (including undefined, a thenable, or a promise).
      - `exception` is a value that is thrown using the throw statement.
      - `reason` is a value that indicates why a promise was rejected.
      - `settled` the final resting state of a promise, fulfilled or rejected.

      A promise can be in one of three states: pending, fulfilled, or rejected.

      Promises that are fulfilled have a fulfillment value and are in the fulfilled
      state.  Promises that are rejected have a rejection reason and are in the
      rejected state.  A fulfillment value is never a thenable.

      Promises can also be said to *resolve* a value.  If this value is also a
      promise, then the original promise's settled state will match the value's
      settled state.  So a promise that *resolves* a promise that rejects will
      itself reject, and a promise that *resolves* a promise that fulfills will
      itself fulfill.


      Basic Usage:
      ------------

      ```js
      var promise = new Promise(function(resolve, reject) {
        // on success
        resolve(value);

        // on failure
        reject(reason);
      });

      promise.then(function(value) {
        // on fulfillment
      }, function(reason) {
        // on rejection
      });
      ```

      Advanced Usage:
      ---------------

      Promises shine when abstracting away asynchronous interactions such as
      `XMLHttpRequest`s.

      ```js
      function getJSON(url) {
        return new Promise(function(resolve, reject){
          var xhr = new XMLHttpRequest();

          xhr.open('GET', url);
          xhr.onreadystatechange = handler;
          xhr.responseType = 'json';
          xhr.setRequestHeader('Accept', 'application/json');
          xhr.send();

          function handler() {
            if (this.readyState === this.DONE) {
              if (this.status === 200) {
                resolve(this.response);
              } else {
                reject(new Error('getJSON: `' + url + '` failed with status: [' + this.status + ']'));
              }
            }
          };
        });
      }

      getJSON('/posts.json').then(function(json) {
        // on fulfillment
      }, function(reason) {
        // on rejection
      });
      ```

      Unlike callbacks, promises are great composable primitives.

      ```js
      Promise.all([
        getJSON('/posts'),
        getJSON('/comments')
      ]).then(function(values){
        values[0] // => postsJSON
        values[1] // => commentsJSON

        return values;
      });
      ```

      @class Promise
      @param {function} resolver
      Useful for tooling.
      @constructor
    */
    function $$es6$promise$promise$$Promise(resolver) {
      this._id = $$es6$promise$promise$$counter++;
      this._state = undefined;
      this._result = undefined;
      this._subscribers = [];

      if ($$$internal$$noop !== resolver) {
        if (!$$utils$$isFunction(resolver)) {
          $$es6$promise$promise$$needsResolver();
        }

        if (!(this instanceof $$es6$promise$promise$$Promise)) {
          $$es6$promise$promise$$needsNew();
        }

        $$$internal$$initializePromise(this, resolver);
      }
    }

    $$es6$promise$promise$$Promise.all = $$promise$all$$default;
    $$es6$promise$promise$$Promise.race = $$promise$race$$default;
    $$es6$promise$promise$$Promise.resolve = $$promise$resolve$$default;
    $$es6$promise$promise$$Promise.reject = $$promise$reject$$default;

    $$es6$promise$promise$$Promise.prototype = {
      constructor: $$es6$promise$promise$$Promise,

    /**
      The primary way of interacting with a promise is through its `then` method,
      which registers callbacks to receive either a promise's eventual value or the
      reason why the promise cannot be fulfilled.

      ```js
      findUser().then(function(user){
        // user is available
      }, function(reason){
        // user is unavailable, and you are given the reason why
      });
      ```

      Chaining
      --------

      The return value of `then` is itself a promise.  This second, 'downstream'
      promise is resolved with the return value of the first promise's fulfillment
      or rejection handler, or rejected if the handler throws an exception.

      ```js
      findUser().then(function (user) {
        return user.name;
      }, function (reason) {
        return 'default name';
      }).then(function (userName) {
        // If `findUser` fulfilled, `userName` will be the user's name, otherwise it
        // will be `'default name'`
      });

      findUser().then(function (user) {
        throw new Error('Found user, but still unhappy');
      }, function (reason) {
        throw new Error('`findUser` rejected and we're unhappy');
      }).then(function (value) {
        // never reached
      }, function (reason) {
        // if `findUser` fulfilled, `reason` will be 'Found user, but still unhappy'.
        // If `findUser` rejected, `reason` will be '`findUser` rejected and we're unhappy'.
      });
      ```
      If the downstream promise does not specify a rejection handler, rejection reasons will be propagated further downstream.

      ```js
      findUser().then(function (user) {
        throw new PedagogicalException('Upstream error');
      }).then(function (value) {
        // never reached
      }).then(function (value) {
        // never reached
      }, function (reason) {
        // The `PedgagocialException` is propagated all the way down to here
      });
      ```

      Assimilation
      ------------

      Sometimes the value you want to propagate to a downstream promise can only be
      retrieved asynchronously. This can be achieved by returning a promise in the
      fulfillment or rejection handler. The downstream promise will then be pending
      until the returned promise is settled. This is called *assimilation*.

      ```js
      findUser().then(function (user) {
        return findCommentsByAuthor(user);
      }).then(function (comments) {
        // The user's comments are now available
      });
      ```

      If the assimliated promise rejects, then the downstream promise will also reject.

      ```js
      findUser().then(function (user) {
        return findCommentsByAuthor(user);
      }).then(function (comments) {
        // If `findCommentsByAuthor` fulfills, we'll have the value here
      }, function (reason) {
        // If `findCommentsByAuthor` rejects, we'll have the reason here
      });
      ```

      Simple Example
      --------------

      Synchronous Example

      ```javascript
      var result;

      try {
        result = findResult();
        // success
      } catch(reason) {
        // failure
      }
      ```

      Errback Example

      ```js
      findResult(function(result, err){
        if (err) {
          // failure
        } else {
          // success
        }
      });
      ```

      Promise Example;

      ```javascript
      findResult().then(function(result){
        // success
      }, function(reason){
        // failure
      });
      ```

      Advanced Example
      --------------

      Synchronous Example

      ```javascript
      var author, books;

      try {
        author = findAuthor();
        books  = findBooksByAuthor(author);
        // success
      } catch(reason) {
        // failure
      }
      ```

      Errback Example

      ```js

      function foundBooks(books) {

      }

      function failure(reason) {

      }

      findAuthor(function(author, err){
        if (err) {
          failure(err);
          // failure
        } else {
          try {
            findBoooksByAuthor(author, function(books, err) {
              if (err) {
                failure(err);
              } else {
                try {
                  foundBooks(books);
                } catch(reason) {
                  failure(reason);
                }
              }
            });
          } catch(error) {
            failure(err);
          }
          // success
        }
      });
      ```

      Promise Example;

      ```javascript
      findAuthor().
        then(findBooksByAuthor).
        then(function(books){
          // found books
      }).catch(function(reason){
        // something went wrong
      });
      ```

      @method then
      @param {Function} onFulfilled
      @param {Function} onRejected
      Useful for tooling.
      @return {Promise}
    */
      then: function(onFulfillment, onRejection) {
        var parent = this;
        var state = parent._state;

        if (state === $$$internal$$FULFILLED && !onFulfillment || state === $$$internal$$REJECTED && !onRejection) {
          return this;
        }

        var child = new this.constructor($$$internal$$noop);
        var result = parent._result;

        if (state) {
          var callback = arguments[state - 1];
          $$asap$$default(function(){
            $$$internal$$invokeCallback(state, child, callback, result);
          });
        } else {
          $$$internal$$subscribe(parent, child, onFulfillment, onRejection);
        }

        return child;
      },

    /**
      `catch` is simply sugar for `then(undefined, onRejection)` which makes it the same
      as the catch block of a try/catch statement.

      ```js
      function findAuthor(){
        throw new Error('couldn't find that author');
      }

      // synchronous
      try {
        findAuthor();
      } catch(reason) {
        // something went wrong
      }

      // async with promises
      findAuthor().catch(function(reason){
        // something went wrong
      });
      ```

      @method catch
      @param {Function} onRejection
      Useful for tooling.
      @return {Promise}
    */
      'catch': function(onRejection) {
        return this.then(null, onRejection);
      }
    };

    var $$es6$promise$polyfill$$default = function polyfill() {
      var local;

      if (typeof global !== 'undefined') {
        local = global;
      } else if (typeof window !== 'undefined' && window.document) {
        local = window;
      } else {
        local = self;
      }

      var es6PromiseSupport =
        "Promise" in local &&
        // Some of these methods are missing from
        // Firefox/Chrome experimental implementations
        "resolve" in local.Promise &&
        "reject" in local.Promise &&
        "all" in local.Promise &&
        "race" in local.Promise &&
        // Older version of the spec had a resolver object
        // as the arg rather than a function
        (function() {
          var resolve;
          new local.Promise(function(r) { resolve = r; });
          return $$utils$$isFunction(resolve);
        }());

      if (!es6PromiseSupport) {
        local.Promise = $$es6$promise$promise$$default;
      }
    };

    var es6$promise$umd$$ES6Promise = {
      'Promise': $$es6$promise$promise$$default,
      'polyfill': $$es6$promise$polyfill$$default
    };

    /* global define:true module:true window: true */
    if (typeof define === 'function' && define['amd']) {
      define(function() { return es6$promise$umd$$ES6Promise; });
    } else if (typeof module !== 'undefined' && module['exports']) {
      module['exports'] = es6$promise$umd$$ES6Promise;
    } else if (typeof this !== 'undefined') {
      this['ES6Promise'] = es6$promise$umd$$ES6Promise;
    }
}).call(this);



/**
 * @module
 * Mashcast 
 */
var mashcastfm = (function (exports) {
    
    /**
     * Abstract class for Mashcast user storage
     **/
    var MashcastBackend = function () {

    };

    /**
     * Logs user in
     * @param {String} username The username
     * @param {String} password The password
     * @return {Promise} a promise 
     **/
    MashcastBackend.prototype.login = function (username, password) {

    }

    /**
     * Adds podcast
     **/
    MashcastBackend.prototype.addPodcast = function (podcast) {

    }
    
    /**
     * Returns if it has podcast
     * @param {String} podcast
     **/
    MashcastBackend.prototype.hasPodcast = function (podcast) {

    }

    exports.onmediaended = function () {
        mashcast.stopEpisode();
        
    }
    
    var AudioApp = function (name, id, options) {
        this.name = name;
        this.id = id;
        this.options = options;
    };
    
    /***
     * Abstract class for getting and setting the user account 
     */
    var Account = function () {
        
    };
    Account.prototype = new Observable();
    Account.prototype.constructor = Observable;
   
    Account.prototype.login = function () {
        
    };
    
    var LocalSettings = function () {

    }
    LocalSettings.prototype.setItem = function (key, value) {
      var config = bungalow_load_settings();
      if (!('localStorage' in config)) {
       config.localStorage = {};
      }
      config.localStorage[key] = JSON.parse(value);
      bungalow_save_settings(config);
    }


    LocalSettings.prototype.getItem = function (key) {
      var config = bungalow_load_settings();
      if (!('localStorage' in config)) {
       config.localStorage = {};
      }
      return JSON.stringify(config.localStorage[key]);
    }
    localSettings = new LocalSettings;
    /**
     * Mashcast base class 
     * @constructor
     * @param {String} audioApp The name of the audio application
     * @this {Mashcast}
     * @implements {Observable}
     * @class
     */
    var READY = 0;
    var PLAYING = 1;
    var Mashcast = function (audioApp) {
        this.podcasts = localSettings.getItem('podcasts') ? JSON.parse(localSettings.getItem('podcasts')) : [];
        this.broadcasts = []; 
        console.log("f", this.podcasts);
        this.date = null;
        this.audioApp = audioApp;
        this.channels = [];
        this.pendingEpisodes = [];
        this.episodes = [];
        this.episode = null;
        this.channel = null;
        this.playing = false;
        this.status = READY;

        var self = this;
  
        this.addEventListener('mediaended', function () {
          console.log("EVENT");
          mashcast.stopEpisode();
          self.episodes = []; // TODO Empty the qeue for now
          self.playing = false;
          
        });
        this.volume = 50;
        
        this.tickert = setInterval(function () {
            if (self.playing) {
                return;
            }
            var episode = self.episodes.shift();
            if (episode != null) {
                self.play(episode);

                if (!isNaN(episode.duration)) {
                  setTimeout(function () {
                    
                    mashcast.stopEpisode();
                    self.episodes = [];

                  }, episode.duration);
                }
            }
        }, 1000);
        this.addEventListener('newepisode', function (e, evt, data) {
            var episode = data.episode;
            // // console.log("Episode", episode);
            if (self.episodes.length == 0 && !self.playing) // TODO Queue doesn't work empty it
            self.episodes.push(episode);
        });



    };

    Mashcast.prototype = new Observable();
    Mashcast.prototype.constructor = Observable;
    

    Mashcast.prototype.hasPodcast = function (podcast) {
      // console.log("podcasts", this.podcasts);
      this.podcasts = JSON.parse(localSettings.getItem('podcasts'));
        var podcasts = this.podcasts.filter(function (object) {
          // console.log(podcast, "Podcast");
           // console.log(podcast, object);
            return object && object.url === podcast.url || object.url == podcast;
        });
        // console.log("Has podcasts", podcast, podcasts);
      //  console.log("podcasts", podcasts);
        return podcasts.length > 0;
    }

    Mashcast.prototype.addPodcast = function (podcast) {
        this.podcasts.push(podcast);
        console.log(this.podcasts);
        localSettings.setItem('podcasts', JSON.stringify(this.podcasts));
    }

    Mashcast.prototype.removePodcast = function (podcast) {
        podcast = this.podcasts.filter(function (a) {
          return podcast.id == a.id;
        })[0]
        this.podcasts.splice(this.podcasts.indexOf(podcast), 1);
        localSettings.setItem('podcasts', JSON.stringify(this.podcasts));
    }

    /**
     * Returns the id of the last peisode for a given podcast with the url
     * @this {Podcast}
     * @function
     * @return {String|null} A string if found, otherwise null.
     */
    Mashcast.prototype.setLatestEpisode = function (episode) {
        return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', episode);
    }
    

    Mashcast.prototype.start = function () {
        // // console.log(this);
        // // console.log("Starting channel");
        var self = this;
        this.ticker = setInterval(function () {
            // // console.log("Checking channel");
            self.checkForNewEpisodes();
        }, 60000);

            self.checkForNewEpisodes();
    }



    Mashcast.prototype.stop = function () {
        clearInterval(this.ticker);
    }


    Mashcast.prototype.checkForNewEpisodes = function () {
        // // console.log("Checking for new episodes");
        var self = this;
       // We don't need CORS header since this will be run inside
        // a CEF-based app.
        console.log("Checking for new episodes");
        this.podcasts.forEach(function (podcast) {
          var url = podcast.url;

          try {
            if (podcast.schedule_url !== '' && podcast.schedule_url !== null) {
              console.log("Checking", podcast.schedule_url);
              // Check schedule
              $.getJSON('/json.php?url=' + encodeURI(podcast.schedule_url), function (data) {
                console.log("Got schedule URL");
                $.each(data.schedule, function (i, episode) {
                  try {
                    console.log("episode.start", episode.start);
                    var start = new Date(Date.parse(episode.start));
                    console.log(episode.duration);
                    console.log(start);
                    var end = new Date(start.getTime() + episode.duration * 1000);

                    var now = new Date();
                    var offset = now.getTime() - start.getTime();
                    console.log("Checking programme ", episode);
                    var d = now.getTime() > start && now.getTime() < end.getTime();
                    console.log(now.getTime() - start);
                    console.log("in program", d);
                    console.log("start", start);
                    console.log("end", end);
                    console.log("now", now);
                    if (d) {
                      console.log("Dispatch live episode", episode);
                      self.dispatchEvent('newepisode', {
                        episode: new Episode({
                            url:episode.schedule_url,
                            duration: (episode.duration - offset)
                        })
                      });
                    }
                  } catch (e) {
                      console.log(e, e.stack);
                  }
                });
              }).fail(function (e) {
                console.log(e)
              });
              return;
            }
              var xmlHttp = new XMLHttpRequest();
              // console.log(url);
              // this.unsetLatestEpisode(url);
              xmlHttp.onreadystatechange = function () {
                  // console.log(xmlHttp.readyState);
                  if (xmlHttp.readyState == 4) {
                      // console.log(xmlHttp.status);
                      // // console.log("Connection start")
                      if (xmlHttp.status == 200) {                          // // console.log("Got data");
                          // // console.log(xmlHttp);
                          var xmlDoc = null;
                          // console.log(xmlHttp.responseText);
                          if (xmlHttp.responseXML != null) {
                              xmlDoc = xmlHttp.responseXML;
                          } else {
                              var parser = new DOMParser();
                              xmlDoc = parser.parseFromString(xmlHttp.responseText, "text/xml");
                              
                          }
                          
                          if (xmlDoc != null) {
                              // // console.log(url);
                             // console.log("TG"); 
                              var latestEpisode = xmlDoc.getElementsByTagName('item')[0];
                              if (!latestEpisode) 
                              {
                                  // console.log("Error");
                                  
                                  return;
                              }
                              var items = xmlDoc.getElementsByTagName('item');
                              for (var i = items.length - 1; i >= 0; i--) {
                                  var episode = items[i];
                                  var pubDate = episode.getElementsByTagName('pubDate')[0].textContent;
                                  pubDate = new Date(pubDate);
                                  var now = mashcast.date != null ? mashcast.date : new Date();
                                  // // console.log(now);
                                  var delta = diff(now.getTime(), pubDate.getTime() );
                              
                                  // // console.log("Difference", delta, pubDate);
                                 
                                  
                                  var url = episode.getElementsByTagName("enclosure")[0].getAttribute('url');
                                  //console.log(url);
                                  // // console.log("Has new episodes", self.getLatestEpisode() != url);
                                  if (delta > -1 && delta < 1000 * 60 * 60 * 2 && played_episodes.indexOf(url) < 0) {
                                      //if (self.getLatestEpisode() == url) {
                                       //   continue;
                                      //}
                                      // console.log("GT");
                                      mashcast.date = null;
                                      // // console.log("New episode found");
                                      // // console.log(self.mashcast);
                                      self.dispatchEvent('newepisode', {
                                              episode: new Episode({
                                                  url:url,
                                                  duration: null
                                              })
                                          
                                      });
                                      played_episodes += url + ';';
                                      
                                      self.setLatestEpisode(url);
                                      break;
                                  }
                                  // We use local storage to check for enw 
                                  
                              }
                            // resolve(url);
                          } else {
                            console.log("No XML recognized");
                          }
                      } else {
                            console.log("No XML recognized");
                          //fail();
                      }
                  }
              };
              // console.log("FL");
              // // console.log("Sending request");
              var url = '/rss.php?url=' + encodeURI(url);
              xmlHttp.open('GET', url, true);
              xmlHttp.send(null);
              // // console.log("Request sent");
          } catch (e) {
              console.log(e);
          }
      }); 
    };

    Mashcast.prototype.getChannelById = function (id) {
        for(var i = 0 ; i < this.channels.length; i++) {
            var channel = this.channels[i];
            if (channel.id == id)
                return channel;
        }
        return null;
    }

    Mashcast.prototype.request = function (method, address, query) {
         var promise = new Promise(function (resolve, fail) {
             var xmlHttp = new XMLHttpRequest();
             xmlHttp.onreadystatechange = function () {
                 if (xmlHttp.readyState == 4) {
                     if (xmlHttp.status == 200) {
                         var json = JSON.parse(xmlHttp.responseText);
                         resolve(json);
                     }
                 }
             };
             xmlHttp.open(method, address + '?' + query);
             xmlHttp.send(query);
         });
         return promise;
         
    };
    
    Mashcast.prototype.loadChannel = function (channel) {
         var self = this;
         var promise = new Promise(function (resolve, fail) {
      
             self.request('GET', 'http://api.radioflow.se/v1/channel/' + channel + '/', 'format=json').then(function (data) {
                var channel = new Channel(data, self, this.mashcast);
                self.channels.push(channel);
                resolve(channel);
                self.channel = channel;
             });
         });
         return promise;
    };
    
    /**
     * Stop (Mute) the music 
     */
    Mashcast.prototype.stopMusic = function () {
        // // console.log("Stopping music");
      var event = new CustomEvent('episodestopped');
      event.data = episode;
      this.dispatchEvent(event);

      return MC.stopMusic(); //this.fadeOutMusic(this.audioApp);
    }
    /**
     * Stop (Mute) the music 
     */
    Mashcast.prototype.startMusic = function () {
        // // console.log("Starting music");
        
        // In this build, we broadcast an event
      var event = new CustomEvent('episodestarted');
      event.data = episode;
      this.dispatchEvent(event);

        //return  MC.playMusic();  //this.fadeInMusic(this.audioApp).then(function (){});
    };
    
    Mashcast.prototype.fadeOutMusic = function (appId) {
        var promise = new Promise(function (resolve, fail) {
            var self = this;
           
            var appId = this.audioApp;
            // // console.log("A");
            // // console.log("Starting fading in music from" + self.volume);
                    if (true) {
                        MC.stopMusic();
                        setTimeout(function () {
                            resolve();
                        }, 100);
            self.status = PLAYING;
            mashcast.playing = true;
                    } else {
                        var ic = setInterval(function () {
                if (self.volume > -1) {
                    self.volume -= 1;
                   } else {
                       MC.stopMusic();
                         resolve(self);
                        clearInterval(ic);
                        __mashcast.showPopup(0, 'Radioflow', 'Music block started');
                        self.status = PLAYING;
                        mashcast.playing = true;
                
                   }
                   // // console.log(self.volume);
                var result = __mashcast.setApplicationVolume(appId, self.volume);     
                if (!result) {
                    clearInterval(ic);
                    fail();
                    __mashcast.showMessage(0, "Could not sync volume, the program will not be played");
                } else {
                    
                }
                // // console.log("setting volume to " + self.volume);
                
                        }, 100);
                    }
        });
        return promise;
    }
    Mashcast.prototype.fadeInMusic = function () {
        var self = this;
        var promise = new Promise(function (resolve, fail) {
            var appId = this.audioApp;
            console.log(MC.play());
          // // console.log("Starting fading in music");
                    MC.play();

          self.status = READY;
          mashcast.playing = false;
            if (false) {
                        var ic = setInterval(function () {
                
                if (self.volume < 50) {
                    self.volume += 1;
                    
                } else { 
                    self.status = READY;
                    mashcast.playing = false;
                    fail(self);
                    clearInterval(ic);
                    
                }
                // // console.log("setting volume to " + self.volume);
                __mashcast.setApplicationVolume(appId, self.volume);
                // // console.log("T");
                
                        }, 100);
                    }
                    setTimeout(function () {
                        resolve(self);
                    }, 100);
        });
        return promise;
    }
    
    Mashcast.prototype.playEpisode = function (episode) {
        var self = this;
       
        
        this.episodes.push(episode);
    }
    
    Mashcast.prototype.play = function (episode) {
        if (this.playing) {
            return;
        }
            if (episode == null) {
                return;
            }
        // // console.log(episode.url);
        var self = this;
        this.stopMusic();
    
    }
    
     Mashcast.prototype.stopEpisode = function (episode) {
        
        this.episode = null;
                this.playing = false;
      /*  if (this.episodes.length > 0) {
            this.play();
        } else {
            this.startMusic(); // Unmute the music
        }*/
       //// alert(this.episodes.length);
       console.log(this.episodes);
        console.log(this.episodes.length);
      // if (this.episodes.length < 1) {
       //   // alert("T");
            this.startMusic();
       //}
    };
    
    /**
     *Queue the episode 
     */
    
    Mashcast.prototype.enqueueEpisode = function (episode) {
        this.episodes.push(episode);  
    };
    
    /** 
     * Channel 
     **/
    Channel = function (channel, mashcast) {
        // // console.log(arguments);
        this.podcasts = [];
        this.name = channel.name;
        this.id = channel.id;
        this.mashcast = mashcast;
        this.data = channel;
        this.status = READY;
         for (var i = 0; i < channel.podcasts.length; i++) {
            var podcast = new Podcast(channel.podcasts[i], this, this.mashcast);
            // // console.log("Podacst", podcast);
            this.podcasts.push(podcast);
            
         }

    };

    
    Channel.prototype = new Observable();
    Channel.prototype.constructor = Observable;

    /***
     * Check for new episodes
     **/
    Channel.prototype.checkForNewEpisodes = function () {
        // console.log(this.podcasts);
        for (var i = 0; i < this.podcasts.length; i++) {
            // console.log("T");
            this.podcasts[i].checkForNewEpisodes();
        }
    };

    /**
     *Activates the channel
     * @function
     * @this {Channel} 
     */
    Channel.prototype.start = function () {
        // // console.log("Starting channel");
        // // console.log("CHANNEL", this);
        // // console.log(this.podcasts);
        for (var i = 0; i < this.podcasts.length; i++) {
            var podcast = this.podcasts[i];
            // // console.log(podcast);
            podcast.start();
        }
        this.status = PLAYING;
        __mashcast.addActiveChannel(this.id);
    }
    
    /**
     * Turns of the channel 
     */
    Channel.prototype.stop = function () {
        for (var i = 0; i < this.podcasts.length; i++) {
            var podcast = this.podcasts[i];
            podcast.stop();
        }
        this.status = READY;
        __mashcast.removeActiveChannel(this.id);
    }
    /*
     * Register a podcast channel into the system
     * @param {Podcast} podcast The podcast to register
     */
    Channel.prototype.registerPodcast = function (podcast) {
        this.podcasts.push(podcast);
        var self = this;
        podcast.addEventListener('newepisode', function (event) {
            self.mashcast.enqueueEpisode(event.data.episode);
        });
    };
    
    /**
     * Unregister a podcast from the system
     * @method
     * @this {Mashcast}
     * @param {Podcast} podcast The podcast to unregister 
     */
    Channel.prototype.unregisterPodcast = function (podcast) {
        this.podcasts.splice(this.podcasts.indexOf(podcast), 1);
    };
    
   
    
    /**
     * Podcast
     * @class
     * @this {Podcast}
     * @constructor
     * @param {String} url The url to the podcast 
     */
    var Podcast = function (podcast, channel, mashcast) {
        this.url = podcast.url;
        this.name = podcast.name;
        this.channel = channel;
        
        this.ticker = null;
        this.mashcast = mashcast;
        // // console.log(mashcast);
    }
    Podcast.prototype = new Observable();
    Podcast.prototype.constructor = Observable;
    
    /***
     * Episode
     * @function
     * @constructor
     * @class
     * @param {String} url The url
     * @param {Mashcast} mashcast The instance of the mashcast object 
     */
    var Episode = function (episode, mashcast) {
        this.url = episode.url;
        this.title = episode.title;
        this.mashcast = mashcast;
    };
    Episode.prototype = new Observable();
    Episode.prototype.constructor = Observable;
    
    /**
     * Starts playing the episode 
     */
    Episode.prototype.play = function () {
        // // console.log(this.url);
        // Play the episode
        this.mashcast.playEpisode(this); // Request mashcast to play the programme
        this.dispatchEvent('episodestarted', {
            data: {
                episode: this
            }
        });
    };
    
     /**
     * Starts playing the episode 
     */
    Episode.prototype.stop = function () {
        // Play the episode
        this.mashcast.stopEpisode(this); // Request mashcast to play the programme
        this.dispatchEvent('episodeended', {
            data: {
                episode: this
            }
        });
    };
    
     /**
     * Returns the id of the last peisode for a given podcast with the url
     * @this {Podcast}
     * @function
     * @return {String|null} A string if found, otherwise null.
     */
    Podcast.prototype.getLatestEpisode = function () {
        return localSettings.getItem('mashcast:podcast:' + this.url + ':episode', null);
    }
    
    /**
     * Returns the id of the last peisode for a given podcast with the url
     * @this {Podcast}
     * @function
     * @return {String|null} A string if found, otherwise null.
     */
    Podcast.prototype.setLatestEpisode = function (episode) {
        return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', episode);
    }
    
    /**
     *Unset latest episode 
     */
    Podcast.prototype.unsetLatestEpisode = function (episode) {
        return localSettings.setItem('mashcast:podcast:' + this.url + ':episode', null);
    }
    
    function diff(x, y) {
        return x - y ? x - y : y - x;
    }
    
    
    var played_episodes = '';
    
    
    /**
     * Check for updates
     * @this {Podcast}
     * @param {Object} url
     * 
     */
    Podcast.prototype.checkForNewEpisodes = function () {
        // // console.log("Checking for new episodes");
        var self = this;
       // We don't need CORS header since this will be run inside
        // a CEF-based app.
        try {
            var xmlHttp = new XMLHttpRequest();
            var url = this.url;
            // console.log(url);
            var self = this;
          if (podcast.stream_url !== '') {
            // Check schedule
            $.getJSON(podcast.schedule_url, function (data) {
              $.each(data.schedule, function (episode) {
                var start = new Date(episode.start);
                var end = new Date(start.getTime() + episode.duration * 1000);
                var now = new Date();
                var offset = now.getTime() - start;
                if (now.getTime() > start && now.getTime() < end) {
                  self.dispatchEvent('newepisode', {
                    episode: new Episode({
                        url:episode.stream_url,
                        duration: episode.duration - offset
                    })
                  });
                }
              });
            });
            return;
          }
            // this.unsetLatestEpisode(url);
            xmlHttp.onreadystatechange = function () {
                // console.log(xmlHttp.readyState);
                if (xmlHttp.readyState == 4) {
                    // console.log(xmlHttp.status);
                    // // console.log("Connection start")
                    if (xmlHttp.status == 200) {
                        // // console.log("Got data");
                        // // console.log(xmlHttp);
                        var xmlDoc = null;
                        // console.log(xmlHttp.responseText);
                        if (xmlHttp.responseXML != null) {
                            xmlDoc = xmlHttp.responseXML;
                        } else {
                            var parser = new DOMParser();
                            xmlDoc = parser.parseFromString(xmlHttp.responseText, "text/xml");
                        }
                        // console.log(xmlDoc);
                        if (xmlDoc != null) {
                            // // console.log(url);
                           console.log(xmlDoc.responseXML);
                           // console.log("TG"); 
                            var latestEpisode = xmlDoc.getElementsByTagName('item')[0];
                            if (!latestEpisode) 
                            {
                                // console.log("Error");
                                
                                return;
                            }
                            var items = xmlDoc.getElementsByTagName('item');
                            for (var i = items.length - 1; i >= 0; i--) {
                                var episode = items[i];
                                var pubDate = episode.getElementsByTagName('pubDate')[0].textContent;
                                pubDate = new Date(pubDate);
                                var now = mashcast.date != null ? mashcast.date : new Date();
                                // // console.log(now);
                                var delta = diff(now.getTime(), pubDate.getTime() );
                            
                                // // console.log("Difference", delta, pubDate);
                               
                                
                                var url = episode.getElementsByTagName("enclosure")[0].getAttribute('url');
                                // // console.log("Has new episodes", self.getLatestEpisode() != url);
                                if (delta > -1 && delta < 1000 * 60 * 60 * 2 && played_episodes.indexOf(url) < 0) {
                                    //if (self.getLatestEpisode() == url) {
                                     //   continue;
                                    //}
                                    // console.log("GT");
                                    mashcast.date = null;
                                    // // console.log("New episode found");
                                    // // console.log(self.mashcast);
                                    self.mashcast.dispatchEvent('newepisode', {
                                            episode: new Episode({
                                                url:url,
                                              duration: null
                                            })
                                        
                                    });
                                    played_episodes += url + ';';
                                    
                                    self.setLatestEpisode(url);
                                    break;
                                }
                                // We use local storage to check for enw 
                                
                            }
                          // resolve(url);
                        } else {
                         //  fail();
                        }
                    } else {
                        // // console.log("Error occured");
                        //fail();
                    }
                }
            };
            // console.log("FL");
            // // console.log("Sending request");
            var url = '/rss.php?url=' + encodeURI(url);
            console.log(url);
            xmlHttp.open('GET', url, true);
            xmlHttp.send(null);
            // // console.log("Request sent");
        } catch (e) {
            // console.log(e.stack);
        }
            
    };
    
    Podcast.prototype.start = function () {
        // // console.log(this);
        // // console.log("Starting channel");
        var self = this;
        this.ticker = setInterval(function () {
            // // console.log("Checking channel");
            self.checkForNewEpisodes();
        }, 60000);
        setTimeout(function () {
            // // console.log("Checking channel");
            self.checkForNewEpisodes();
        }, 1000);
    };
    Podcast.prototype.stop = function () {
        clearInterval(this.ticker);
    };
    exports.Mashcast = Mashcast;
    exports.Podcast = Podcast;
    exports.AudioApp = AudioApp;
    exports.Account = Account;
    exports.Channel = Channel;
    exports.Episode = Episode;

    /* global define:true module:true window: true */
    if (typeof define === 'function' && define['amd']) {
      define(function() { return exports; });
    } else if (typeof module !== 'undefined' && module['exports']) {
      module['exports'] = exports;
    } else if (typeof this !== 'undefined') {
      this['ES6Promise'] = exports;
    }
});
mashcastfm(window);