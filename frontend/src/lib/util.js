import lodash from 'lodash';
import moment from 'moment';
import React from 'react';

import Gantt from '../components/Gantt';

export function avgStat(stats) {
  return _.round(_.sum(stats) / _.size(stats), 3);
}

export function toTime(x) {
  return moment({m: x}).toDate();
}

export function shortTime(x) {
  return moment({h:Math.floor(x/60), m:x%60}).format('HH:mm');
}

export function ganttHelper(state, animation, stats) {
  function textCb(p) {
    let ret = '';
    if (p.volunteer) ret += 'v';
    if (p.arrival < state.time && p.begin > state.time) ret += 'w';
    if (p.originalSite != p.site) ret += 'M';
      return ret;
  }

  function tipCb(d) {
    const w = Math.round(stats[d.name] * 100) / 100;
    let ret = `
      ${d.name} wait ${w} <br/>
      appointment ${shortTime(d.appointment)} <br/>
      slot ${d.slot} <br/>
      `;
    if (d.arrival < state.time) ret += `arrival ${shortTime(d.arrival)} <br/>`;
    if (d.begin < state.time) ret += `begin ${shortTime(d.begin)} machine ${d.machine} <br/>`;
    if (d.completion < state.time) ret += `completion ${shortTime(d.completion)} <br/>`;
    return ret;
  }

  const nameScale = d3.scale.category10();
  const waitScale = d3.scale.linear().domain([0, 90])
  .range(["green", "red"]);


  return <Gantt
    data={animation.patients}
    begin={d => toTime(d.begin)}
    end={d => toTime(d.completion)}
    row={d => d.site + " " + d.machine}
    xlim={[toTime(360), toTime(1380)]}
    time={toTime(state.time)}
    stroke={d => nameScale(d.name)}
    tip={tipCb}
    color={d => waitScale(stats[d.name])}
    text={textCb}
  />
}
