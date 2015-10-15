import React from 'react/addons';
import d3 from 'd3';

import Gantt from './Gantt';
import {toTime, shortTime} from '../lib/util';

export default React.createClass({
  render() {
    function tipCb(d) {
      return `
      ${d.name} class ${d.clazz} <br/>
      appointment ${shortTime(d.appointment)} <br/>
      arrival ${shortTime(d.arrival)} <br/>
      begin ${shortTime(d.begin)} <br/>
      completion ${shortTime(d.completion)} <br/>
      duration ${d.completion - d.begin} slot ${d.slot} <br/>
      `;
    }

    const nameScale = d3.scale.category10();
    const waitScale = d3.scale.linear().domain([0, 90]).range(["green", "red"]);

    return (
      <div style={{marginTop: 90}}>
        <button onClick={this.props.handleToConfig}>Back to Config</button>
        <Gantt
          data={this.props.schedule}
          begin={d => toTime(d.begin)}
          end={d => toTime(d.completion)}
          row={d => d.site + " " + d.machine}
          xlim={[toTime(360), toTime(1380)]}
          stroke={d => waitScale(d.arrival - d.appointment)}
          color={d => waitScale(d.begin - Math.max(d.arrival, d.appointment))}
          tip={tipCb}
        />
      </div>
    );
  }
});
