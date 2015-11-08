import React from 'react/addons';

import {avgStat, toTime, ganttHelper} from '../lib/util';

export default React.createClass({
  getInitialState() {
    return {id: 0};
  },
  render() {
    const frames = this.props.frames;
    const id = this.state.id;
    const frame = frames[id];

    return (
        <div>
          <h1>Animation</h1>
          <button onClick={this.props.handleToConfig}>Go Config</button>
          <input type='range'
          min='0' max={frames.length-1} value={id}
          onChange={ evt=>
            this.setState({
              id: parseInt(evt.target.value)
            })
          }
          style={{width: '300px'}}
          />

          <div>
            Avg waiting time: {avgStat(frame.stats)}
          </div>

          {ganttHelper(frame.time, frame.animation, frame.stats)}
        </div>
    );
  }
});
