import flask

from get_replay import get_replay
from get_history import get_history

app = flask.Flask(__name__)
app.debug = True

@app.route('/parse_replay', methods=['POST'])
def parse_replay():
    config = flask.request.get_json()
    state = get_replay(config)
    return flask.jsonify(state)

@app.route('/parse_history', methods=['POST'])
def parse_history():
    config = flask.request.get_json()
    ret = get_history(config)
    return flask.jsonify(ret)

app.run(host='0.0.0.0')
