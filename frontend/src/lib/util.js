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

export function ganttHelper(time, animation, stats) {
  function textCb(p) {
    let ret = '';
    if (p.schedule==null) ret += 'S';
    if (p.volunteer) ret += 'v';
    if (p.arrival < time && p.begin > time) ret += 'w';
    if (p.originalSite != p.site) ret += 'M';
      return ret;
  }

  function tipCb(d) {
    const w = stats.wait[d.name];
    let ret = `
      ${d.name} wait ${w} <br/>
      ${d.clazz} <br/>
      appointment ${shortTime(d.appointment)} <br/>
      slot ${d.slot} <br/>
      `;
    if (d.arrival < time) ret += `arrival ${shortTime(d.arrival)} <br/>`;
    if (d.begin < time) ret += `begin ${shortTime(d.begin)} machine ${d.machine} <br/>`;
    if (d.completion < time) ret += `completion ${shortTime(d.completion)} <br/>`;
    return ret;
  }

  const nameScale = d3.scale.category10();
  const waitScale = d3.scale.linear().domain([0, 90])
  .range(["green", "red"]);


  return <Gantt
    data={animation}
    begin={d => toTime(d.begin)}
    end={d => toTime(d.completion)}
    row={d => d.site + " " + d.machine}
    xlim={[toTime(360), toTime(1380)]}
    time={toTime(time)}
    stroke={d => nameScale(d.name)}
    tip={tipCb}
    color={d => waitScale(stats.wait[d.name])}
    text={textCb}
  />
}
